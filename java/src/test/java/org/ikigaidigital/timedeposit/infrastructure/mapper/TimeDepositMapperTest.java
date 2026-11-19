package org.ikigaidigital.timedeposit.infrastructure.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.WithAssertions;
import org.ikigaidigital.timedeposit.domain.model.TimeDepositDetails;
import org.ikigaidigital.timedeposit.domain.model.Withdrawal;
import org.ikigaidigital.timedeposit.infrastructure.entity.projection.TimeDepositWithdrawalRow;
import org.junit.jupiter.api.Test;

class TimeDepositMapperTest implements WithAssertions {

  private static final LocalDate JANUARY_15 = LocalDate.of(2026, 1, 15);
  private static final LocalDate MARCH_1 = LocalDate.of(2026, 3, 1);

  @Test
  void shouldGroupRowsIntoOneDepositWithAllItsWithdrawals() {
    // given
    List<TimeDepositWithdrawalRow> rows = List.of(
        row(10, "basic", "100.00", 45, 2, "10.00", JANUARY_15),
        row(10, "basic", "100.00", 45, 1, "30.00", MARCH_1));

    // when
    List<TimeDepositDetails> deposits = TimeDepositMapper.toDetails(rows);

    // then
    assertThat(deposits).usingRecursiveFieldByFieldElementComparator().containsExactly(
        new TimeDepositDetails(10, "basic", new BigDecimal("100.00"), 45, List.of(
            new Withdrawal(2, new BigDecimal("10.00"), JANUARY_15),
            new Withdrawal(1, new BigDecimal("30.00"), MARCH_1))));
  }

  @Test
  void shouldGiveEmptyWithdrawalsToDepositWithoutWithdrawals() {
    // given: LEFT JOIN row with null withdrawal columns
    List<TimeDepositWithdrawalRow> rows = List.of(rowWithoutWithdrawal(20, "student", "200.00", 100));

    // when
    List<TimeDepositDetails> deposits = TimeDepositMapper.toDetails(rows);

    // then
    assertThat(deposits).singleElement().satisfies(deposit -> {
      assertThat(deposit.id()).isEqualTo(20);
      assertThat(deposit.withdrawals()).isEmpty();
    });
  }

  @Test
  void shouldKeepOrderOfDepositsAndWithdrawalsFromRows() {
    // given: deposit ids deliberately not ascending, to prove the mapper keeps row order
    List<TimeDepositWithdrawalRow> rows = List.of(
        row(30, "premium", "300.00", 60, 7, "70.00", MARCH_1),
        row(30, "premium", "300.00", 60, 4, "40.00", JANUARY_15),
        rowWithoutWithdrawal(10, "basic", "100.00", 45),
        row(20, "student", "200.00", 100, 5, "50.00", JANUARY_15));

    // when
    List<TimeDepositDetails> deposits = TimeDepositMapper.toDetails(rows);

    // then
    assertThat(deposits).extracting(TimeDepositDetails::id).containsExactly(30, 10, 20);
    assertThat(deposits.getFirst().withdrawals()).extracting(Withdrawal::id).containsExactly(7, 4);
  }

  @Test
  void shouldReturnNoDepositsForNoRows() {
    // given when
    List<TimeDepositDetails> deposits = TimeDepositMapper.toDetails(List.of());

    // then
    assertThat(deposits).isEmpty();
  }

  private static TimeDepositWithdrawalRow row(int depositId, String planType, String balance, int days,
      int withdrawalId, String amount, LocalDate date) {
    return new TimeDepositWithdrawalRow(depositId, planType, new BigDecimal(balance), days,
        withdrawalId, new BigDecimal(amount), date);
  }

  private static TimeDepositWithdrawalRow rowWithoutWithdrawal(int depositId, String planType, String balance,
      int days) {
    return new TimeDepositWithdrawalRow(depositId, planType, new BigDecimal(balance), days, null, null, null);
  }
}
