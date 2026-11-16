package org.ikigaidigital;

import org.ikigaidigital.domain.interest.InterestPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TimeDepositCalculatorTest {

    private static final List<String> PLAN_TYPES = List.of("basic", "student", "premium", "unknown");
    private static final List<Integer> DEPOSIT_DAYS = List.of(0, 1, 29, 30, 31, 44, 45, 46, 100, 364, 365, 366, 367, 1000);
    private static final List<Double> BALANCES = List.of(
            0.0, 0.01, 0.5, 1.0, 99.99, 333.33, 1000.0, 1234.56, 99999.99, 1234567.0, 10000000.55);

    private final TimeDepositCalculator timeDepositCalculator = new TimeDepositCalculator();

    static Stream<Arguments> legacyComparisonCases() {
        List<Arguments> comparisonCases = new ArrayList<>();
        for (String planType : PLAN_TYPES) {
            for (int depositDays : DEPOSIT_DAYS) {
                for (double balance : BALANCES) {
                    comparisonCases.add(Arguments.of(planType, depositDays, balance));
                }
            }
        }
        return comparisonCases.stream();
    }

    @ParameterizedTest(name = "{0} plan, {1} days, balance {2}")
    @MethodSource("legacyComparisonCases")
    @DisplayName("Should update balance exactly like the legacy calculator")
    void shouldUpdateBalanceExactlyLikeLegacyCalculator(String planType, int depositDays, double balance) {
        TimeDeposit refactoredTimeDeposit = new TimeDeposit(1, planType, balance, depositDays);
        TimeDeposit legacyTimeDeposit = new TimeDeposit(1, planType, balance, depositDays);

        timeDepositCalculator.updateBalance(List.of(refactoredTimeDeposit));
        new LegacyTimeDepositCalculator().updateBalance(List.of(legacyTimeDeposit));

        assertThat(refactoredTimeDeposit.getBalance()).isEqualTo(legacyTimeDeposit.getBalance());
    }

    @ParameterizedTest(name = "{0} plan, {1} days, balance {2} -> {3}")
    @CsvSource({
            "basic, 45, 1234567.00, 1235595.81",
            "basic, 30, 10000.00, 10000.00",
            "basic, 31, 10000.00, 10008.33",
            "student, 200, 20000.00, 20050.00",
            "student, 365, 20000.00, 20050.00",
            "student, 366, 20000.00, 20000.00",
            "premium, 45, 30000.00, 30000.00",
            "premium, 46, 30000.00, 30125.00",
            "premium, 120, 1234567.00, 1239711.03",
            "unknown, 100, 5000.00, 5000.00"
    })
    @DisplayName("Should apply monthly interest according to plan type and deposit days")
    void shouldApplyMonthlyInterestAccordingToPlanTypeAndDepositDays(
            String planType, int depositDays, double balance, double expectedBalance) {
        TimeDeposit timeDeposit = new TimeDeposit(1, planType, balance, depositDays);

        timeDepositCalculator.updateBalance(List.of(timeDeposit));

        assertThat(timeDeposit.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("Should update every time deposit in the list")
    void shouldUpdateEveryTimeDepositInList() {
        List<TimeDeposit> timeDeposits = List.of(
                new TimeDeposit(1, "basic", 10000.00, 45),
                new TimeDeposit(2, "student", 20000.00, 200),
                new TimeDeposit(3, "premium", 30000.00, 46));

        timeDepositCalculator.updateBalance(timeDeposits);

        assertThat(timeDeposits)
                .extracting(TimeDeposit::getBalance)
                .containsExactly(10008.33, 20050.00, 30125.00);
    }

    @Test
    @DisplayName("Should apply interest of a newly added plan without changing the calculator")
    void shouldApplyInterestOfNewlyAddedPlan() {
        InterestPolicy flatBonusInterestPolicy = new InterestPolicy() {
            @Override
            public String planType() {
                return "bonus";
            }

            @Override
            public double calculateMonthlyInterest(TimeDeposit timeDeposit) {
                return 10.0;
            }
        };
        TimeDepositCalculator calculatorWithBonusPlan = new TimeDepositCalculator(List.of(flatBonusInterestPolicy));
        TimeDeposit timeDeposit = new TimeDeposit(1, "bonus", 100.00, 1);

        calculatorWithBonusPlan.updateBalance(List.of(timeDeposit));

        assertThat(timeDeposit.getBalance()).isEqualTo(110.00);
    }
}
