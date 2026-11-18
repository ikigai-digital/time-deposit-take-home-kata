package org.ikigaidigital.timedeposit.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.WithAssertions;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.ikigaidigital.timedeposit.TestcontainersConfiguration;
import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.WithdrawalEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.projection.TimeDepositWithdrawalRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TimeDepositJpaRepositoryTest implements WithAssertions {

  private static final String BASIC = "basic";
  private static final String STUDENT = "student";
  private static final String PREMIUM = "premium";
  private static final String ID = "id";
  private static final int UNKNOWN_ID = 404;
  private static final LocalDate JANUARY_15 = LocalDate.of(2026, 1, 15);
  private static final LocalDate MARCH_1 = LocalDate.of(2026, 3, 1);

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

  @Test
  void shouldReturnFirstPageAsOneRowPerWithdrawal() {
    // given
    seedPagingFixture();

    // when
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(0, 2);

    // then: deposit 20 has no withdrawals but still appears once (LEFT JOIN)
    assertThat(rows)
        .extracting(TimeDepositWithdrawalRow::depositId, TimeDepositWithdrawalRow::withdrawalId)
        .containsExactly(tuple(10, 2), tuple(10, 3), tuple(10, 1), tuple(20, null));
  }

  @Test
  void shouldSelectPageByRowPositionNotById() {
    // given
    seedPagingFixture();

    // when: page 1 of size 2 = deposits at positions 3..4, i.e. ids 30 and 40 (ids have gaps)
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(2, 4);

    // then
    assertThat(rows)
        .extracting(TimeDepositWithdrawalRow::depositId, TimeDepositWithdrawalRow::withdrawalId)
        .containsExactly(tuple(30, 4), tuple(40, 5), tuple(40, 6));
  }

  @Test
  void shouldReturnPartialLastPage() {
    // given
    seedPagingFixture();

    // when
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(4, 6);

    // then
    assertThat(rows)
        .extracting(TimeDepositWithdrawalRow::depositId, TimeDepositWithdrawalRow::withdrawalId)
        .containsExactly(tuple(50, null));
  }

  @Test
  void shouldReturnNothingBeyondLastPage() {
    // given
    seedPagingFixture();

    // when
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(6, 8);

    // then
    assertThat(rows).isEmpty();
  }

  @Test
  void shouldReturnNothingWhenTableIsEmpty() {
    // given empty table

    // when
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(0, 20);

    // then
    assertThat(rows).isEmpty();
  }

  @Test
  void shouldCountDepositsNotJoinedRowsInPageSize() {
    // given: deposit 10 alone produces three joined rows
    seedPagingFixture();

    // when: page size 1
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(0, 1);

    // then: all three rows of deposit 10, none cut off by the page size, nothing of deposit 20
    assertThat(rows)
        .extracting(TimeDepositWithdrawalRow::depositId, TimeDepositWithdrawalRow::withdrawalId)
        .containsExactly(tuple(10, 2), tuple(10, 3), tuple(10, 1));
  }

  @Test
  void shouldOrderRowsByDepositIdThenWithdrawalDateThenId() {
    // given: withdrawal 1 is the latest of deposit 10; 2 and 3 share a date
    seedPagingFixture();

    // when: one page holding everything
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(0, 10);

    // then
    assertThat(rows)
        .extracting(TimeDepositWithdrawalRow::depositId, TimeDepositWithdrawalRow::withdrawalId)
        .containsExactly(
            tuple(10, 2), tuple(10, 3), tuple(10, 1),
            tuple(20, null),
            tuple(30, 4),
            tuple(40, 5), tuple(40, 6),
            tuple(50, null));
  }

  @Test
  void shouldMapAllDepositAndWithdrawalColumns() {
    // given
    seedPagingFixture();

    // when
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(3, 4);   // deposit 40

    // then
    assertThat(rows).usingRecursiveFieldByFieldElementComparator().containsExactly(
        new TimeDepositWithdrawalRow(40, PREMIUM, new BigDecimal("400.00"), 90, 5, new BigDecimal("50.00"), JANUARY_15),
        new TimeDepositWithdrawalRow(40, PREMIUM, new BigDecimal("400.00"), 90, 6, new BigDecimal("60.00"), MARCH_1));
  }

  @Test
  void shouldReturnNullWithdrawalColumnsForDepositWithoutWithdrawals() {
    // given
    seedPagingFixture();

    // when
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(1, 2);   // deposit 20

    // then
    assertThat(rows).singleElement().satisfies(row -> {
      assertThat(row.depositId()).isEqualTo(20);
      assertThat(row.planType()).isEqualTo(STUDENT);
      assertThat(row.hasWithdrawal()).isFalse();
      assertThat(row.withdrawalId()).isNull();
      assertThat(row.amount()).isNull();
      assertThat(row.date()).isNull();
    });
  }

  @Test
  void shouldLoadPageInExactlyOneSqlStatement() {
    // given
    seedPagingFixture();
    Statistics statistics = hibernateStatistics();
    statistics.clear();

    // when
    List<TimeDepositWithdrawalRow> rows = timeDepositRepository.findPageRows(0, 5);

    // then: 5 deposits and 6 withdrawals in one statement - no N+1
    assertThat(rows).hasSize(8);
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
  }

  private void seedPagingFixture() {
    timeDepositRepository.saveAllAndFlush(List.of(
        deposit(50, BASIC, 45, "500.00"), deposit(10, BASIC, 45, "100.00"),
        deposit(30, PREMIUM, 60, "300.00"), deposit(20, STUDENT, 100, "200.00"),
        deposit(40, PREMIUM, 90, "400.00")));
    entityManager.persist(withdrawal(1, 10, "30.00", MARCH_1));
    entityManager.persist(withdrawal(2, 10, "10.00", JANUARY_15));
    entityManager.persist(withdrawal(3, 10, "20.00", JANUARY_15));
    entityManager.persist(withdrawal(4, 30, "40.00", JANUARY_15));
    entityManager.persist(withdrawal(5, 40, "50.00", JANUARY_15));
    entityManager.persist(withdrawal(6, 40, "60.00", MARCH_1));
    entityManager.flush();
    entityManager.clear();
  }

  private Statistics hibernateStatistics() {
    return entityManager.getEntityManager().getEntityManagerFactory()
        .unwrap(SessionFactory.class)
        .getStatistics();
  }

  private static TimeDepositEntity deposit(int id, String planType, int days, String balance) {
    return new TimeDepositEntity(id, planType, days, new BigDecimal(balance));
  }

  private static WithdrawalEntity withdrawal(int id, int timeDepositId, String amount, LocalDate date) {
    return new WithdrawalEntity(id, timeDepositId, new BigDecimal(amount), date);
  }
}
