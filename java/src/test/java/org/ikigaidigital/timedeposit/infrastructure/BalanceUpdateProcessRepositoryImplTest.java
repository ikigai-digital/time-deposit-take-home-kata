package org.ikigaidigital.timedeposit.infrastructure;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.assertj.core.api.WithAssertions;
import org.ikigaidigital.timedeposit.TestcontainersConfiguration;
import org.ikigaidigital.timedeposit.infrastructure.entity.BalanceUpdateProcessEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.enums.ProcessStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, BalanceUpdateProcessRepositoryImpl.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BalanceUpdateProcessRepositoryImplTest implements WithAssertions {

  private static final long UNKNOWN_PROCESS_ID = 404L;

  @Autowired
  private BalanceUpdateProcessRepositoryImpl processes;

  @Autowired
  private BalanceUpdateProcessJpaRepository jpaRepository;

  @BeforeEach
  void cleanTable() {
    jpaRepository.deleteAllInBatch();
  }

  @Test
  void shouldStartProcessInProgress() {
    // given empty table

    // when
    Optional<Long> processId = processes.tryStart();

    // then
    assertThat(processId).isPresent();
    BalanceUpdateProcessEntity process = load(processId.get());
    assertThat(process.getStatus()).isEqualTo(ProcessStatusEntity.IN_PROGRESS);
    assertThat(process.getStartedAt()).isNotNull();
    assertThat(process.getFinishedAt()).isNull();
  }

  @Test
  void shouldNotStartSecondProcessWhileOneIsInProgress() {
    // given
    Optional<Long> running = processes.tryStart();

    // when
    Optional<Long> second = processes.tryStart();

    // then
    assertThat(running).isPresent();
    assertThat(second).isEmpty();
    assertThat(jpaRepository.count()).isEqualTo(1);
  }

  @Test
  void shouldStartNewProcessAfterPreviousCompleted() {
    // given
    long previous = processes.tryStart().orElseThrow();
    processes.markCompleted(previous);

    // when
    Optional<Long> next = processes.tryStart();

    // then
    assertThat(next).isPresent().get().isNotEqualTo(previous);
    assertThat(load(next.get()).getStatus()).isEqualTo(ProcessStatusEntity.IN_PROGRESS);
  }

  @Test
  void shouldStartNewProcessAfterPreviousFailed() {
    // given
    long previous = processes.tryStart().orElseThrow();
    processes.markFailed(previous);

    // when
    Optional<Long> next = processes.tryStart();

    // then
    assertThat(next).isPresent().get().isNotEqualTo(previous);
  }

  @Test
  void shouldMarkProcessCompleted() {
    // given
    long processId = processes.tryStart().orElseThrow();

    // when
    processes.markCompleted(processId);

    // then
    BalanceUpdateProcessEntity process = load(processId);
    assertThat(process.getStatus()).isEqualTo(ProcessStatusEntity.COMPLETED);
    assertThat(process.getFinishedAt()).isNotNull().isAfterOrEqualTo(process.getStartedAt());
  }

  @Test
  void shouldMarkProcessFailed() {
    // given
    long processId = processes.tryStart().orElseThrow();

    // when
    processes.markFailed(processId);

    // then
    BalanceUpdateProcessEntity process = load(processId);
    assertThat(process.getStatus()).isEqualTo(ProcessStatusEntity.FAILED);
    assertThat(process.getFinishedAt()).isNotNull().isAfterOrEqualTo(process.getStartedAt());
  }

  @Test
  void shouldFinishOnlyTheGivenProcess() {
    // given
    long completed = processes.tryStart().orElseThrow();
    processes.markCompleted(completed);
    long running = processes.tryStart().orElseThrow();

    // when
    processes.markFailed(running);

    // then
    assertThat(load(completed).getStatus()).isEqualTo(ProcessStatusEntity.COMPLETED);
    assertThat(load(running).getStatus()).isEqualTo(ProcessStatusEntity.FAILED);
  }

  @Test
  void shouldIgnoreUnknownProcessId() {
    // given empty table

    // when
    Throwable thrown = catchThrowable(() -> {
      processes.markCompleted(UNKNOWN_PROCESS_ID);
      processes.markFailed(UNKNOWN_PROCESS_ID);
    });

    // then
    assertThat(thrown).isNull();
    assertThat(jpaRepository.count()).isZero();
  }

  @Test
  void shouldStartExactlyOneProcessWhenCalledConcurrently() throws Exception {
    // given
    int callers = 16;
    CountDownLatch startSignal = new CountDownLatch(1);
    Callable<Optional<Long>> tryStart = () -> {
      startSignal.await();
      return processes.tryStart();
    };

    // when
    long started;
    try (ExecutorService pool = Executors.newFixedThreadPool(callers)) {
      List<Future<Optional<Long>>> results = Collections.nCopies(callers, tryStart).stream()
          .map(pool::submit)
          .toList();
      startSignal.countDown();
      started = 0;
      for (Future<Optional<Long>> result : results) {
        started += result.get().isPresent() ? 1 : 0;
      }
    }

    // then
    assertThat(started).isEqualTo(1);
    assertThat(jpaRepository.count()).isEqualTo(1);
  }

  private BalanceUpdateProcessEntity load(long processId) {
    return jpaRepository.findById(processId).orElseThrow();
  }
}
