package org.ikigaidigital.timedeposit.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.WithAssertions;
import org.ikigaidigital.timedeposit.TestcontainersConfiguration;
import org.ikigaidigital.timedeposit.infrastructure.entity.BalanceUpdateProcessEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.enums.ProcessStatusEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class BalanceUpdateProcessJpaRepositoryTest implements WithAssertions {

  private static final ProcessStatusEntity IN_PROGRESS = ProcessStatusEntity.IN_PROGRESS;
  private static final ProcessStatusEntity COMPLETED = ProcessStatusEntity.COMPLETED;
  private static final ProcessStatusEntity FAILED = ProcessStatusEntity.FAILED;
  private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 11, 18, 10, 0);
  private static final LocalDateTime FINISHED_AT = LocalDateTime.of(2026, 11, 18, 10, 5);
  private static final LocalDateTime EARLIER_FINISHED_AT = LocalDateTime.of(2026, 11, 17, 9, 0);
  private static final long UNKNOWN_ID = 404L;
  private static final String ID = "id";

  @Autowired
  private BalanceUpdateProcessJpaRepository processRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void shouldPersistAllColumns() {
    // given
    BalanceUpdateProcessEntity process = process(COMPLETED, STARTED_AT, FINISHED_AT);

    // when
    BalanceUpdateProcessEntity saved = processRepository.saveAndFlush(process);
    entityManager.clear();

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(load(saved.getId()))
        .usingRecursiveComparison()
        .ignoringFields(ID)
        .isEqualTo(process(COMPLETED, STARTED_AT, FINISHED_AT));
  }

  @Test
  void shouldCreateProcessInProgressWithoutFinishTime() {
    // given
    BalanceUpdateProcessEntity process = BalanceUpdateProcessEntity.inProgress();

    // when
    Long id = processRepository.saveAndFlush(process).getId();
    entityManager.clear();

    // then
    BalanceUpdateProcessEntity stored = load(id);
    assertThat(stored.getStatus()).isEqualTo(IN_PROGRESS);
    assertThat(stored.getStartedAt()).isNotNull();
    assertThat(stored.getFinishedAt()).isNull();
  }

  @Test
  void shouldRejectSecondProcessInProgress() {
    // given
    processRepository.saveAndFlush(BalanceUpdateProcessEntity.inProgress());

    // when
    Throwable thrown = catchThrowable(
        () -> processRepository.saveAndFlush(BalanceUpdateProcessEntity.inProgress()));

    // then: the partial unique index allows only one IN_PROGRESS row
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldAllowManyFinishedProcessesNextToOneInProgress() {
    // given
    List<BalanceUpdateProcessEntity> processes = List.of(
        process(COMPLETED, STARTED_AT, FINISHED_AT),
        process(COMPLETED, STARTED_AT, FINISHED_AT),
        process(FAILED, STARTED_AT, FINISHED_AT),
        process(FAILED, STARTED_AT, FINISHED_AT),
        process(IN_PROGRESS, STARTED_AT, null));

    // when
    processRepository.saveAllAndFlush(processes);

    // then
    assertThat(processRepository.count()).isEqualTo(5);
  }

  @Test
  void shouldFinishProcess() {
    // given
    Long id = processRepository.saveAndFlush(process(IN_PROGRESS, STARTED_AT, null)).getId();

    // when
    processRepository.finish(id, COMPLETED, FINISHED_AT);

    // then
    assertThat(load(id))
        .extracting(BalanceUpdateProcessEntity::getStatus, BalanceUpdateProcessEntity::getStartedAt,
            BalanceUpdateProcessEntity::getFinishedAt)
        .containsExactly(COMPLETED, STARTED_AT, FINISHED_AT);
  }

  @Test
  void shouldFinishOnlyGivenProcess() {
    // given
    Long earlier = processRepository.saveAndFlush(
        process(COMPLETED, STARTED_AT, EARLIER_FINISHED_AT)).getId();
    Long running = processRepository.saveAndFlush(process(IN_PROGRESS, STARTED_AT, null)).getId();

    // when
    processRepository.finish(running, FAILED, FINISHED_AT);

    // then
    assertThat(load(running))
        .extracting(BalanceUpdateProcessEntity::getStatus, BalanceUpdateProcessEntity::getFinishedAt)
        .containsExactly(FAILED, FINISHED_AT);
    assertThat(load(earlier))
        .extracting(BalanceUpdateProcessEntity::getStatus, BalanceUpdateProcessEntity::getFinishedAt)
        .containsExactly(COMPLETED, EARLIER_FINISHED_AT);
  }

  @Test
  void shouldDoNothingWhenFinishingUnknownProcess() {
    // given
    Long id = processRepository.saveAndFlush(process(IN_PROGRESS, STARTED_AT, null)).getId();

    // when
    Throwable thrown = catchThrowable(
        () -> processRepository.finish(UNKNOWN_ID, COMPLETED, FINISHED_AT));

    // then
    assertThat(thrown).isNull();
    assertThat(load(id).getStatus()).isEqualTo(IN_PROGRESS);
    assertThat(processRepository.count()).isEqualTo(1);
  }


  @Test
  void shouldRejectProcessWithoutStatus() {
    // given
    BalanceUpdateProcessEntity withoutStatus = process(null, STARTED_AT, null);

    // when
    Throwable thrown = catchThrowable(() -> processRepository.saveAndFlush(withoutStatus));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRejectProcessWithoutStartTime() {
    // given
    BalanceUpdateProcessEntity withoutStartTime = process(IN_PROGRESS, null, null);

    // when
    Throwable thrown = catchThrowable(() -> processRepository.saveAndFlush(withoutStartTime));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  private BalanceUpdateProcessEntity load(Long id) {
    return processRepository.findById(id).orElseThrow();
  }

  private static BalanceUpdateProcessEntity process(ProcessStatusEntity status,
      LocalDateTime startedAt, LocalDateTime finishedAt) {
    return new BalanceUpdateProcessEntity(null, status, startedAt, finishedAt);
  }
}
