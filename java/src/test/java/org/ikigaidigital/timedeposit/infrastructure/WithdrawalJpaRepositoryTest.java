package org.ikigaidigital.timedeposit.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.WithAssertions;
import org.ikigaidigital.timedeposit.TestcontainersConfiguration;
import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.WithdrawalEntity;
import org.junit.jupiter.api.BeforeEach;
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
class WithdrawalJpaRepositoryTest implements WithAssertions {

  private static final int DEPOSIT_ID = 1;
  private static final int OTHER_DEPOSIT_ID = 2;
  private static final int UNKNOWN_DEPOSIT_ID = 404;
  private static final LocalDate JANUARY_15 = LocalDate.of(2026, 1, 15);
  private static final LocalDate FEBRUARY_1 = LocalDate.of(2026, 2, 1);
  private static final String ID = "id";

  @Autowired
  private WithdrawalJpaRepository withdrawalRepository;

  @Autowired
  private TimeDepositJpaRepository timeDepositRepository;

  @Autowired
  private TestEntityManager entityManager;

  @BeforeEach
  void storeDeposits() {
    timeDepositRepository.saveAllAndFlush(List.of(
        new TimeDepositEntity(DEPOSIT_ID, "basic", 45, new BigDecimal("1000.00")),
        new TimeDepositEntity(OTHER_DEPOSIT_ID, "premium", 60, new BigDecimal("5000.00"))));
  }

  @Test
  void shouldPersistAllColumns() {
    // given
    WithdrawalEntity withdrawal = withdrawal(1, DEPOSIT_ID, "100.00", JANUARY_15);

    // when
    withdrawalRepository.saveAndFlush(withdrawal);
    entityManager.clear();

    // then
    assertThat(withdrawalRepository.findById(1)).get()
        .usingRecursiveComparison()
        .isEqualTo(withdrawal(1, DEPOSIT_ID, "100.00", JANUARY_15));
  }

  @Test
  void shouldFindWithdrawalsOfAllDepositsSortedById() {
    // given
    withdrawalRepository.saveAllAndFlush(List.of(
        withdrawal(3, OTHER_DEPOSIT_ID, "30.00", FEBRUARY_1),
        withdrawal(1, DEPOSIT_ID, "10.00", JANUARY_15),
        withdrawal(2, DEPOSIT_ID, "20.50", FEBRUARY_1)));
    entityManager.clear();

    // when
    List<WithdrawalEntity> withdrawals = withdrawalRepository.findAll(Sort.by(ID));

    // then
    assertThat(withdrawals)
        .extracting(WithdrawalEntity::getId, WithdrawalEntity::getTimeDepositId,
            WithdrawalEntity::getAmount)
        .containsExactly(
            tuple(1, DEPOSIT_ID, new BigDecimal("10.00")),
            tuple(2, DEPOSIT_ID, new BigDecimal("20.50")),
            tuple(3, OTHER_DEPOSIT_ID, new BigDecimal("30.00")));
  }

  @Test
  void shouldFindNoWithdrawalsWhenTableIsEmpty() {
    // given when
    List<WithdrawalEntity> withdrawals = withdrawalRepository.findAll();

    // then
    assertThat(withdrawals).isEmpty();
  }

  @Test
  void shouldStoreAmountWithTwoDecimalPlaces() {
    // given
    WithdrawalEntity withdrawal = withdrawal(1, DEPOSIT_ID, "12.345", JANUARY_15);

    // when
    withdrawalRepository.saveAndFlush(withdrawal);
    entityManager.clear();

    // then
    assertThat(withdrawalRepository.findById(1)).get()
        .extracting(WithdrawalEntity::getAmount).isEqualTo(new BigDecimal("12.35"));
  }

  @Test
  void shouldDeleteWithdrawalAndKeepDeposit() {
    // given
    withdrawalRepository.saveAndFlush(withdrawal(1, DEPOSIT_ID, "100.00", JANUARY_15));

    // when
    withdrawalRepository.deleteById(1);
    withdrawalRepository.flush();

    // then
    assertThat(withdrawalRepository.existsById(1)).isFalse();
    assertThat(timeDepositRepository.existsById(DEPOSIT_ID)).isTrue();
  }

  @Test
  void shouldRejectWithdrawalOfUnknownDeposit() {
    // given
    WithdrawalEntity ofUnknownDeposit = withdrawal(1, UNKNOWN_DEPOSIT_ID, "100.00", JANUARY_15);

    // when
    Throwable thrown = catchThrowable(() -> withdrawalRepository.saveAndFlush(ofUnknownDeposit));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRejectWithdrawalWithoutTimeDeposit() {
    // given
    WithdrawalEntity withoutDeposit = withdrawal(1, null, "100.00", JANUARY_15);

    // when
    Throwable thrown = catchThrowable(() -> withdrawalRepository.saveAndFlush(withoutDeposit));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRejectWithdrawalWithoutAmount() {
    // given
    WithdrawalEntity withoutAmount = new WithdrawalEntity(1, DEPOSIT_ID, null, JANUARY_15);

    // when
    Throwable thrown = catchThrowable(() -> withdrawalRepository.saveAndFlush(withoutAmount));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRejectWithdrawalWithoutDate() {
    // given
    WithdrawalEntity withoutDate = withdrawal(1, DEPOSIT_ID, "100.00", null);

    // when
    Throwable thrown = catchThrowable(() -> withdrawalRepository.saveAndFlush(withoutDate));

    // then
    assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
  }

  private static WithdrawalEntity withdrawal(int id, Integer timeDepositId, String amount,
      LocalDate date) {
    return new WithdrawalEntity(id, timeDepositId, new BigDecimal(amount), date);
  }
}
