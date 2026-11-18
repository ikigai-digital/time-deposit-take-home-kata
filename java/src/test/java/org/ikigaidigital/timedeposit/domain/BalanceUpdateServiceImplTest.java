package org.ikigaidigital.timedeposit.domain;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.assertj.core.api.WithAssertions;
import org.ikigaidigital.timedeposit.config.BalanceUpdateProperties;
import org.ikigaidigital.timedeposit.domain.interest.InterestCalculatorImpl;
import org.ikigaidigital.timedeposit.domain.interest.policy.BasicInterestPolicy;
import org.ikigaidigital.timedeposit.domain.interest.policy.PremiumInterestPolicy;
import org.ikigaidigital.timedeposit.domain.interest.policy.StudentInterestPolicy;
import org.ikigaidigital.timedeposit.domain.model.PageResult;
import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
import org.ikigaidigital.timedeposit.domain.model.TimeDepositDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BalanceUpdateServiceImplTest implements WithAssertions {

  private static final long PROCESS_ID = 42L;
  private static final long TIMEOUT_MS = 5_000;
  private static final int CHUNK_SIZE = 3;
  private static final int THREADS = 2;
  private static final String CHUNK_THREAD_PREFIX = "test-chunk-";

  private final InMemoryTimeDepositRepository timeDeposits = new InMemoryTimeDepositRepository();
  private final BalanceUpdateProcessRepository processes = mock(BalanceUpdateProcessRepository.class);
  private final TimeDepositCalculator calculator = new TimeDepositCalculator(new InterestCalculatorImpl());
  private final ExecutorService chunkExecutor = Executors.newFixedThreadPool(THREADS,
      Thread.ofPlatform().name(CHUNK_THREAD_PREFIX, 0).factory());

  private final BalanceUpdateServiceImpl service = new BalanceUpdateServiceImpl(
      timeDeposits, processes, calculator, chunkExecutor, new BalanceUpdateProperties(CHUNK_SIZE, THREADS));

  @AfterEach
  void shutdownExecutor() {
    timeDeposits.openGate();
    chunkExecutor.shutdownNow();
  }

  @Test
  void shouldNotStartUpdateWhenAnotherIsInProgress() {
    // given
    given(processes.tryStart()).willReturn(Optional.empty());
    seedDeposits(7);

    // when
    Optional<Long> processId = service.startUpdate();

    // then
    assertThat(processId).isEmpty();
    assertThat(timeDeposits.idLoads()).isZero();
    assertThat(timeDeposits.processedChunks()).isEmpty();
    verify(processes, never()).markCompleted(anyLong());
    verify(processes, never()).markFailed(anyLong());
  }

  @Test
  void shouldApplyInterestToAllDepositsInChunksAndMarkProcessCompleted() {
    // given
    given(processes.tryStart()).willReturn(Optional.of(PROCESS_ID));
    List<TimeDeposit> expected = seedDeposits(7);
    calculator.updateBalance(expected);

    // when
    Optional<Long> processId = service.startUpdate();

    // then
    assertThat(processId).contains(PROCESS_ID);
    verify(processes, timeout(TIMEOUT_MS)).markCompleted(PROCESS_ID);
    verify(processes, never()).markFailed(anyLong());
    assertThat(timeDeposits.processedChunks())
        .containsExactlyInAnyOrder(List.of(1, 2, 3), List.of(4, 5, 6), List.of(7));
    expected.forEach(deposit -> assertThat(timeDeposits.balanceOf(deposit.getId()))
        .as("balance of deposit %d", deposit.getId())
        .isEqualTo(deposit.getBalance()));
  }

  @Test
  void shouldReturnBeforeUpdateFinishes() {
    // given
    given(processes.tryStart()).willReturn(Optional.of(PROCESS_ID));
    seedDeposits(7);
    timeDeposits.closeGate();                       // chunks block until the gate opens

    // when
    Optional<Long> processId = service.startUpdate();

    // then
    assertThat(processId).contains(PROCESS_ID);
    verify(processes, after(200).never()).markCompleted(anyLong());
    timeDeposits.openGate();
    verify(processes, timeout(TIMEOUT_MS)).markCompleted(PROCESS_ID);
  }

  @Test
  void shouldProcessChunksOnChunkExecutorWithLimitedParallelism() {
    // given
    given(processes.tryStart()).willReturn(Optional.of(PROCESS_ID));
    seedDeposits(30);                               // 10 chunks
    timeDeposits.slowDownChunksBy(50);

    // when
    service.startUpdate();

    // then
    verify(processes, timeout(TIMEOUT_MS)).markCompleted(PROCESS_ID);
    assertThat(timeDeposits.chunkThreadNames()).allMatch(name -> name.startsWith(CHUNK_THREAD_PREFIX));
    assertThat(timeDeposits.maxChunksInParallel()).isGreaterThan(1).isLessThanOrEqualTo(THREADS);
  }

  @Test
  void shouldMarkProcessCompletedWhenThereAreNoDeposits() {
    // given
    given(processes.tryStart()).willReturn(Optional.of(PROCESS_ID));

    // when
    service.startUpdate();

    // then
    verify(processes, timeout(TIMEOUT_MS)).markCompleted(PROCESS_ID);
    assertThat(timeDeposits.processedChunks()).isEmpty();
  }

  @Test
  void shouldMarkProcessFailedWhenAChunkFails() {
    // given
    given(processes.tryStart()).willReturn(Optional.of(PROCESS_ID));
    seedDeposits(7);
    double balanceBefore = timeDeposits.balanceOf(5);
    timeDeposits.failChunkContaining(5);

    // when
    service.startUpdate();

    // then
    verify(processes, timeout(TIMEOUT_MS)).markFailed(PROCESS_ID);
    verify(processes, never()).markCompleted(anyLong());
    assertThat(timeDeposits.balanceOf(5)).isEqualTo(balanceBefore);
  }

  @Test
  void shouldMarkProcessFailedWhenLoadingDepositIdsFails() {
    // given
    given(processes.tryStart()).willReturn(Optional.of(PROCESS_ID));
    timeDeposits.failLoadingIds();

    // when
    service.startUpdate();

    // then
    verify(processes, timeout(TIMEOUT_MS)).markFailed(PROCESS_ID);
    verify(processes, never()).markCompleted(anyLong());
  }

  private List<TimeDeposit> seedDeposits(int count) {
    String[] plans = {BasicInterestPolicy.PLAN_TYPE, StudentInterestPolicy.PLAN_TYPE,
        PremiumInterestPolicy.PLAN_TYPE};
    List<TimeDeposit> copies = new ArrayList<>();
    for (int id = 1; id <= count; id++) {
      TimeDeposit deposit = new TimeDeposit(id, plans[id % plans.length], 1000.0 + id * 100, 60 + id);
      timeDeposits.add(deposit);
      copies.add(InMemoryTimeDepositRepository.copy(deposit));
    }
    return copies;
  }

  private static final class InMemoryTimeDepositRepository implements TimeDepositRepository {

    private final Map<Integer, TimeDeposit> deposits = new ConcurrentHashMap<>();
    private final Queue<List<Integer>> processedChunks = new ConcurrentLinkedQueue<>();
    private final Queue<String> chunkThreadNames = new ConcurrentLinkedQueue<>();
    private final AtomicInteger idLoads = new AtomicInteger();
    private final AtomicInteger runningChunks = new AtomicInteger();
    private final AtomicInteger maxChunksInParallel = new AtomicInteger();
    private volatile CountDownLatch gate = new CountDownLatch(0);
    private volatile long chunkDelayMs;
    private volatile Integer failingId;
    private volatile boolean failLoadingIds;

    @Override
    public List<Integer> getTimeDepositIds() {
      idLoads.incrementAndGet();
      if (failLoadingIds) {
        throw new IllegalStateException("simulated failure while loading ids");
      }
      return deposits.keySet().stream().sorted().toList();
    }

    @Override
    public void updateBalance(List<Integer> ids, Consumer<List<TimeDeposit>> balanceUpdater) {
      int running = runningChunks.incrementAndGet();
      maxChunksInParallel.accumulateAndGet(running, Math::max);
      try {
        chunkThreadNames.add(Thread.currentThread().getName());
        awaitGate();
        sleep(chunkDelayMs);
        if (failingId != null && ids.contains(failingId)) {
          throw new IllegalStateException("simulated failure for chunk " + ids);
        }
        List<TimeDeposit> chunk = ids.stream().map(deposits::get).map(InMemoryTimeDepositRepository::copy).toList();
        balanceUpdater.accept(chunk);
        chunk.forEach(deposit -> deposits.put(deposit.getId(), deposit));
        processedChunks.add(ids);
      } finally {
        runningChunks.decrementAndGet();
      }
    }

    @Override
    public PageResult<TimeDepositDetails> findPage(int page, int size) {
      return null;
    }

    void add(TimeDeposit deposit) {
      deposits.put(deposit.getId(), copy(deposit));
    }

    double balanceOf(int id) {
      return deposits.get(id).getBalance();
    }

    List<List<Integer>> processedChunks() {
      return List.copyOf(processedChunks);
    }

    List<String> chunkThreadNames() {
      return List.copyOf(chunkThreadNames);
    }

    int idLoads() {
      return idLoads.get();
    }

    int maxChunksInParallel() {
      return maxChunksInParallel.get();
    }

    void closeGate() {
      gate = new CountDownLatch(1);
    }

    void openGate() {
      gate.countDown();
    }

    void slowDownChunksBy(long millis) {
      chunkDelayMs = millis;
    }

    void failChunkContaining(int id) {
      failingId = id;
    }

    void failLoadingIds() {
      failLoadingIds = true;
    }

    static TimeDeposit copy(TimeDeposit deposit) {
      return new TimeDeposit(deposit.getId(), deposit.getPlanType(), deposit.getBalance(), deposit.getDays());
    }

    private void awaitGate() {
      try {
        if (!gate.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
          throw new IllegalStateException("gate was never opened");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(e);
      }
    }

    private static void sleep(long millis) {
      if (millis <= 0) {
        return;
      }
      try {
        Thread.sleep(millis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(e);
      }
    }
  }
}
