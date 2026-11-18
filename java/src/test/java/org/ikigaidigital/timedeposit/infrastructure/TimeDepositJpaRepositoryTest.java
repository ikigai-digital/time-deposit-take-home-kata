package org.ikigaidigital.timedeposit.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.WithAssertions;
import org.ikigaidigital.timedeposit.TestcontainersConfiguration;
import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.WithdrawalEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TimeDepositJpaRepositoryTest implements WithAssertions {

  private static final String BASIC = "basic";
  private static final String STUDENT = "student";
  private static final String PREMIUM = "premium";
  private static final String ID = "id";
  private static final int UNKNOWN_ID = 404;

  @Autowired
  private TimeDepositJpaRepository timeDepositRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void shouldPersistAllColumns() {
    // given
    TimeDepositEntity deposit = deposit(1, BASIC, 45, "1234567.00");

    // when
    timeDepositRepository.saveAndFlush(deposit);
    entityManager.clear();

    // then
    assertThat(timeDepositRepository.findById(1)).get()
        .usingRecursiveComparison()
        .isEqualTo(deposit(1, BASIC, 45, "1234567.00"));
  }

  @Test
  void shouldFindAllDepositsSortedById() {
    // given
    timeDepositRepository.saveAllAndFlush(List.of(
        deposit(3, PREMIUM, 60, "300.00"), deposit(1, BASIC, 45, "100.00"),
        deposit(2, STUDENT, 400, "200.00")));
    entityManager.clear();

    // when
    List<TimeDepositEntity> deposits = timeDepositRepository.findAll(Sort.by(ID));

    // then
    assertThat(deposits)
        .extracting(TimeDepositEntity::getId, TimeDepositEntity::getPlanType)
        .containsExactly(tuple(1, BASIC), tuple(2, STUDENT), tuple(3, PREMIUM));
  }

  @Test
  void shouldFindNoDepositsWhenTableIsEmpty() {
    // given when
    List<TimeDepositEntity> deposits = timeDepositRepository.findAll();

    // then
    assertThat(deposits).isEmpty();
  }

  @Test
  void shouldFindNothingForUnknownId() {
    // given
    timeDepositRepository.saveAndFlush(deposit(1, BASIC, 45, "100.00"));

    // when
    Optional<TimeDepositEntity> deposit = timeDepositRepository.findById(UNKNOWN_ID);

    // then
    assertThat(deposit).isEmpty();
  }

  @Test
  void shouldFindOnlyExistingDepositsById() {
    // given
    timeDepositRepository.saveAllAndFlush(
        List.of(deposit(1, BASIC, 45, "100.00"), deposit(2, STUDENT, 40, "200.00")));

    // when
    List<TimeDepositEntity> deposits = timeDepositRepository.findAllById(List.of(1, UNKNOWN_ID));

    // then
    assertThat(deposits).extracting(TimeDepositEntity::getId).containsExactly(1);
  }

  @Test
  void shouldUpdateBalanceOfExistingDeposit() {
    // given
    timeDepositRepository.saveAndFlush(deposit(1, BASIC, 45, "1000.00"));
    entityManager.clear();
    TimeDepositEntity stored = timeDepositRepository.findById(1).orElseThrow();
    stored.setBalance(new BigDecimal("1000.83"));

    // when
    timeDepositRepository.saveAndFlush(stored);
    entityManager.clear();

    // then
    assertThat(timeDepositRepository.findById(1)).get()
        .extracting(TimeDepositEntity::getBalance).isEqualTo(new BigDecimal("1000.83"));
    assertThat(timeDepositRepository.count()).isEqualTo(1);
  }

  @Test
  void shouldStoreBalanceWithTwoDecimalPlaces() {
    // given
    TimeDepositEntity deposit = deposit(1, PREMIUM, 60, "10041.666");

    // when
    timeDepositRepository.saveAndFlush(deposit);
    entityManager.clear();

    // then
    assertThat(timeDepositRepository.findById(1)).get()
        .extracting(TimeDepositEntity::getBalance).isEqualTo(new BigDecimal("10041.67"));
  }

  @Test
  void shouldDeleteDeposit() {
    // given
    timeDepositRepository.saveAndFlush(deposit(1, BASIC, 45, "100.00"));

    // when
    timeDepositRepository.deleteById(1);
    timeDepositRepository.flush();

    // then
    assertThat(timeDepositRepository.existsById(1)).isFalse();
  }

  @Test
  void shouldRejectDeletingDepositWithWithdrawals() {
    // given
    TimeDepositEntity deposit = timeDepositRepository.saveAndFlush(deposit(1, BASIC, 45, "100.00"));
    entityManager.persistAndFlush(
        new WithdrawalEntity(1, 1, new BigDecimal("10.00"), LocalDate.of(2026, 1, 15)));

    // when
    Throwable thrown = catchThrowable(() -> {
      timeDepositRepository.delete(deposit);
      timeDepositRepository.flush();
    });

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRejectDepositWithoutPlanType() {
    // given
    TimeDepositEntity withoutPlanType = deposit(1, null, 45, "100.00");

    // when
    Throwable thrown = catchThrowable(() -> timeDepositRepository.saveAndFlush(withoutPlanType));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRejectDepositWithoutBalance() {
    // given
    TimeDepositEntity withoutBalance = new TimeDepositEntity(1, BASIC, 45, null);

    // when
    Throwable thrown = catchThrowable(() -> timeDepositRepository.saveAndFlush(withoutBalance));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRejectDepositWithoutDays() {
    // given
    TimeDepositEntity withoutDays = new TimeDepositEntity(1, BASIC, null, new BigDecimal("100.00"));

    // when
    Throwable thrown = catchThrowable(() -> timeDepositRepository.saveAndFlush(withoutDays));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  private static TimeDepositEntity deposit(int id, String planType, int days, String balance) {
    return new TimeDepositEntity(id, planType, days, new BigDecimal(balance));
  }
}
