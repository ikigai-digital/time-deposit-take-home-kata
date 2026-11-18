package org.ikigaidigital.timedeposit.domain;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.WithAssertions;
import org.ikigaidigital.timedeposit.domain.interest.calculator.InterestCalculatorImpl;
import org.ikigaidigital.timedeposit.domain.interest.calculator.policy.BasicInterestPolicy;
import org.ikigaidigital.timedeposit.domain.interest.calculator.policy.PremiumInterestPolicy;
import org.ikigaidigital.timedeposit.domain.interest.calculator.policy.StudentInterestPolicy;
import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


class TimeDepositCalculatorTest implements WithAssertions {

  private static final String BASIC = BasicInterestPolicy.PLAN_TYPE;
  private static final String STUDENT = StudentInterestPolicy.PLAN_TYPE;
  private static final String PREMIUM = PremiumInterestPolicy.PLAN_TYPE;
  private static final String UNKNOWN_PLAN = "gold";
  private static final double BALANCE = 1000.00;

  private final TimeDepositCalculator calculator = new TimeDepositCalculator(
      new InterestCalculatorImpl());

  static Stream<Arguments> monthlyInterestCases() {
    return Stream.of(
        // no interest for the first 30 days, any plan
        arguments(BASIC, 0, 1000.00, 1000.00),
        arguments(BASIC, 30, 1000.00, 1000.00),
        arguments(STUDENT, 30, 1000.00, 1000.00),
        arguments(PREMIUM, 30, 1000.00, 1000.00),
        // basic: 1% p.a. -> 1000 * 0.01 / 12 = 0.8333 -> 0.83
        arguments(BASIC, 31, 1000.00, 1000.83),
        arguments(BASIC, 45, 1000.00, 1000.83),
        arguments(BASIC, 366, 1000.00, 1000.83),
        arguments(BASIC, 1000, 1000.00, 1000.83),
        // student: 3% p.a. -> 2.50, only while days < 366
        arguments(STUDENT, 31, 1000.00, 1002.50),
        arguments(STUDENT, 365, 1000.00, 1002.50),
        arguments(STUDENT, 366, 1000.00, 1000.00),
        arguments(STUDENT, 1000, 1000.00, 1000.00),
        // premium: 5% p.a. -> 4.1667 -> 4.17, only when days > 45
        arguments(PREMIUM, 31, 1000.00, 1000.00),
        arguments(PREMIUM, 45, 1000.00, 1000.00),
        arguments(PREMIUM, 46, 1000.00, 1004.17),
        arguments(PREMIUM, 1000, 1000.00, 1004.17),
        // interest rounded HALF_UP to 2 dp: 1234567 * 0.01 / 12 = 1028.8058 -> 1028.81
        arguments(BASIC, 45, 1234567.00, 1235595.81),
        // HALF_UP is applied to the exact binary value of the double (new BigDecimal(double)):
        arguments(PREMIUM, 46, 30.00, 30.13),     // 0.125 exactly -> rounds up to 0.13
        arguments(BASIC, 31, 18.00,
            18.01),       // "0.015" is 0.01499999... in binary -> rounds down to 0.01
        arguments(BASIC, 31, 6.00,
            6.01),         // "0.005" is 0.00500000...01 in binary -> rounds up to 0.01
        arguments(BASIC, 31, 0.60, 0.60),         // interest below half a cent -> 0.00
        // zero and negative balances are not validated
        arguments(BASIC, 31, 0.00, 0.00),
        arguments(BASIC, 31, -1000.00, -1000.83), // HALF_UP rounds away from zero: -0.8333 -> -0.83
        // unknown, empty or differently-cased plan types earn nothing
        arguments(UNKNOWN_PLAN, 100, 1000.00, 1000.00),
        arguments(BASIC.toUpperCase(), 100, 1000.00, 1000.00),
        arguments("", 100, 1000.00, 1000.00));
  }

  @ParameterizedTest(name = "{0}, {1} days: {2} -> {3}")
  @MethodSource("monthlyInterestCases")
  void shouldAppliesMonthlyInterestPerPlan(String planType, int days, double balance,
      double expected) {

    //given
    TimeDeposit deposit = new TimeDeposit(1, planType, balance, days);

    //when
    calculator.updateBalance(List.of(deposit));

    //then
    assertThat(deposit.getBalance()).isEqualTo(expected);
  }

  @Test
  void shouldUpdatesEveryDepositInTheList() {
    //given
    List<TimeDeposit> deposits = List.of(
        new TimeDeposit(1, BASIC, BALANCE, 31),
        new TimeDeposit(2, STUDENT, BALANCE, 31),
        new TimeDeposit(3, PREMIUM, BALANCE, 46),
        new TimeDeposit(4, PREMIUM, BALANCE, 10));

    //when
    calculator.updateBalance(deposits);

    //then
    assertThat(deposits).extracting(TimeDeposit::getBalance)
        .containsExactly(1000.83, 1002.50, 1004.17, 1000.00);
  }

  @Test
  void shouldDoesNotChangeIdPlanTypeOrDays() {
    //given
    TimeDeposit deposit = new TimeDeposit(7, BASIC, BALANCE, 31);

    //when
    calculator.updateBalance(List.of(deposit));

    //then
    assertThat(deposit.getId()).isEqualTo(7);
    assertThat(deposit.getPlanType()).isEqualTo(BASIC);
    assertThat(deposit.getDays()).isEqualTo(31);
  }

  @Test
  void shouldCompoundsWhenCalledRepeatedly() {
    //given
    TimeDeposit deposit = new TimeDeposit(1, BASIC, BALANCE, 31);

    //when
    calculator.updateBalance(List.of(deposit));
    calculator.updateBalance(List.of(deposit));

    //then
    assertThat(deposit.getBalance()).isEqualTo(1001.6600000000001);
  }

  @Test
  void shouldReturnEmptyListWhenNoTermDeposits() {
    //given when then
    assertThatNoException().isThrownBy(() -> calculator.updateBalance(List.of()));
  }

  @Test
  void shouldThrowExceptionWhenNullPlanTypeAfter30Days() {
    //given
    List<TimeDeposit> deposits = List.of(new TimeDeposit(1, null, BALANCE, 31));

    //when then
    assertThatThrownBy(() -> calculator.updateBalance(deposits)).isInstanceOf(
        NullPointerException.class);
  }

  @Test
  void shouldThrowExceptionWhenNullBalance() {
    //given
    List<TimeDeposit> deposits = List.of(new TimeDeposit(1, BASIC, null, 10));

    //when then
    assertThatThrownBy(() -> calculator.updateBalance(deposits)).isInstanceOf(
        NullPointerException.class);
  }

  @Test
  void shouldNullPlanTypeWithin30DaysIsIgnored() {
    //given
    TimeDeposit deposit = new TimeDeposit(1, null, BALANCE, 30);

    //when
    calculator.updateBalance(List.of(deposit));

    //then
    assertThat(deposit.getBalance()).isEqualTo(BALANCE);
  }
}
