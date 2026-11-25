package org.ikigaidigital;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Characterization tests for {@link TimeDepositCalculator#updateBalance(List)}.
 * <p>
 * These tests pin down the CURRENT behaviour of the legacy implementation (which the
 * requirements declare to be correct). Expected values were captured by running the
 * existing code, not derived from the specification. Any refactoring must keep them green.
 * <p>
 * Balances are compared with exact {@code double} equality on purpose: the goal is to
 * detect any change in output, including floating-point artefacts.
 */
class TimeDepositCalculatorTest {

    private static final double BALANCE = 10_000.00;

    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    private double balanceAfterUpdate(String planType, double balance, int days) {
        TimeDeposit deposit = new TimeDeposit(1, planType, balance, days);
        calculator.updateBalance(List.of(deposit));
        return deposit.getBalance();
    }

    @Nested
    @DisplayName("Basic plan: 1% p.a. paid monthly, after 30 days")
    class BasicPlan {

        @ParameterizedTest(name = "days={0} -> balance={1}")
        @CsvSource({
                "0,    10000.00",
                "30,   10000.00",
                "31,   10008.33",
                "45,   10008.33",
                "46,   10008.33",
                "365,  10008.33",
                "366,  10008.33",
                "1000, 10008.33",
        })
        void appliesInterestOnlyAfterDay30(int days, double expectedBalance) {
            assertThat(balanceAfterUpdate("basic", BALANCE, days)).isEqualTo(expectedBalance);
        }
    }

    @Nested
    @DisplayName("Student plan: 3% p.a. paid monthly, from day 31 up to day 365")
    class StudentPlan {

        @ParameterizedTest(name = "days={0} -> balance={1}")
        @CsvSource({
                "0,    10000.00",
                "30,   10000.00",
                "31,   10025.00",
                "45,   10025.00",
                "46,   10025.00",
                "365,  10025.00",
                "366,  10000.00",
                "1000, 10000.00",
        })
        void appliesInterestOnlyWithinFirstYearAfterDay30(int days, double expectedBalance) {
            assertThat(balanceAfterUpdate("student", BALANCE, days)).isEqualTo(expectedBalance);
        }
    }

    @Nested
    @DisplayName("Premium plan: 5% p.a. paid monthly, after 45 days")
    class PremiumPlan {

        @ParameterizedTest(name = "days={0} -> balance={1}")
        @CsvSource({
                "0,    10000.00",
                "30,   10000.00",
                "31,   10000.00",
                "45,   10000.00",
                "46,   10041.67",
                "365,  10041.67",
                "366,  10041.67",
                "1000, 10041.67",
        })
        void appliesInterestOnlyAfterDay45(int days, double expectedBalance) {
            assertThat(balanceAfterUpdate("premium", BALANCE, days)).isEqualTo(expectedBalance);
        }
    }

    @Nested
    @DisplayName("Unrecognised plan types earn no interest")
    class UnknownPlanType {

        @ParameterizedTest(name = "planType=\"{0}\"")
        @CsvSource({
                "Basic",    // matching is case-sensitive
                "STUDENT",
                "Premium",
                "gold",
                "''",       // empty string
        })
        void leavesBalanceUnchanged(String planType) {
            assertThat(balanceAfterUpdate(planType, BALANCE, 100)).isEqualTo(BALANCE);
        }
    }

    @Nested
    @DisplayName("Null values (invalid input, legacy behaviour pinned)")
    class NullValues {

        @ParameterizedTest(name = "days={0}")
        @CsvSource({"0", "30"})
        void nullPlanTypeIsIgnoredWithinInterestFreePeriod(int days) {
            // Legacy code never read planType for days <= 30.
            assertThat(balanceAfterUpdate(null, BALANCE, days)).isEqualTo(BALANCE);
        }

        @ParameterizedTest(name = "days={0}")
        @CsvSource({"31", "46", "366"})
        void nullPlanTypeFailsAfterInterestFreePeriod(int days) {
            assertThatNullPointerException().isThrownBy(() -> balanceAfterUpdate(null, BALANCE, days));
        }

        @ParameterizedTest(name = "days={0}")
        @CsvSource({"0", "31", "100"})
        void nullBalanceAlwaysFails(int days) {
            TimeDeposit deposit = new TimeDeposit(1, "basic", null, days);

            assertThatNullPointerException().isThrownBy(() -> calculator.updateBalance(List.of(deposit)));
        }
    }

    @Nested
    @DisplayName("Rounding of interest")
    class Rounding {

        @Test
        void roundsInterestDownWhenBelowHalfCent() {
            // 10000 * 0.01 / 12 = 8.3333... -> 8.33
            assertThat(balanceAfterUpdate("basic", BALANCE, 100)).isEqualTo(10_008.33);
        }

        @Test
        void roundsInterestUpWhenAboveHalfCent() {
            // 10000 * 0.05 / 12 = 41.6666... -> 41.67
            assertThat(balanceAfterUpdate("premium", BALANCE, 100)).isEqualTo(10_041.67);
        }

        @Test
        void roundsExactHalfCentUp() {
            // 150 * 0.01 / 12 = 0.125 (exactly representable as double) -> 0.13 (HALF_UP)
            assertThat(balanceAfterUpdate("basic", 150.00, 100)).isEqualTo(150.13);
        }

        @ParameterizedTest(name = "{0} balance={1} -> {2}")
        @CsvSource({
                // interest looks like a .xx5 tie in decimal, but the double is slightly below it
                "basic,   18.00, 18.01",   // 0.015 is really 0.01499999... -> 0.01 (BigDecimal.valueOf would give 0.02)
                "basic,   90.00, 90.07",   // 0.075 -> 0.07
                "student,  6.00,  6.01",   // 0.015 -> 0.01
                "premium, 25.20, 25.30",   // 0.105 -> 0.10
        })
        void roundsExactBinaryValueOfInterestNotItsDecimalRepresentation(
                String planType, double balance, double expectedBalance) {
            // Pins the use of new BigDecimal(double) rather than BigDecimal.valueOf(double).
            assertThat(balanceAfterUpdate(planType, balance, 100)).isEqualTo(expectedBalance);
        }

        @Test
        void interestRoundingToZeroLeavesBalanceUnchanged() {
            // 0.60 * 0.01 / 12 = 0.0005 -> 0.00
            assertThat(balanceAfterUpdate("basic", 0.60, 100)).isEqualTo(0.60);
        }

        @ParameterizedTest(name = "balance={0} -> {1}")
        @CsvSource({
                "6.02, 6.029999999999999",
                "6.06, 6.069999999999999",
                "6.10, 6.109999999999999",
        })
        void resultingBalanceIsNotRoundedAndMayCarryFloatingPointArtefacts(double balance, double expectedBalance) {
            // Only the interest is rounded; the sum balance + interest is plain double arithmetic.
            assertThat(balanceAfterUpdate("basic", balance, 100)).isEqualTo(expectedBalance);
        }
    }

    @Nested
    @DisplayName("Collection handling and side effects")
    class CollectionHandling {

        @Test
        void updatesEveryDepositInTheListIndependently() {
            TimeDeposit basic = new TimeDeposit(1, "basic", BALANCE, 31);
            TimeDeposit student = new TimeDeposit(2, "student", BALANCE, 366);
            TimeDeposit premium = new TimeDeposit(3, "premium", BALANCE, 46);
            TimeDeposit young = new TimeDeposit(4, "premium", BALANCE, 30);

            calculator.updateBalance(Arrays.asList(basic, student, premium, young));

            assertThat(basic.getBalance()).isEqualTo(10_008.33);
            assertThat(student.getBalance()).isEqualTo(10_000.00);
            assertThat(premium.getBalance()).isEqualTo(10_041.67);
            assertThat(young.getBalance()).isEqualTo(10_000.00);
        }

        @Test
        void handlesLargeBalance() {
            // 1234567 * 0.01 / 12 = 1028.80583... -> 1028.81
            assertThat(balanceAfterUpdate("basic", 1_234_567.00, 45)).isEqualTo(1_235_595.81);
        }

        @Test
        void zeroBalanceStaysZero() {
            assertThat(balanceAfterUpdate("premium", 0.0, 100)).isEqualTo(0.0);
        }

        @Test
        void emptyListIsANoOp() {
            List<TimeDeposit> deposits = new ArrayList<>();

            calculator.updateBalance(deposits);

            assertThat(deposits).isEmpty();
        }

        @Test
        void onlyBalanceIsMutated() {
            TimeDeposit deposit = new TimeDeposit(7, "premium", BALANCE, 100);

            calculator.updateBalance(List.of(deposit));

            assertThat(deposit.getId()).isEqualTo(7);
            assertThat(deposit.getPlanType()).isEqualTo("premium");
            assertThat(deposit.getDays()).isEqualTo(100);
        }

        @Test
        void eachCallAppliesOneMonthOfInterestAndCompounds() {
            TimeDeposit deposit = new TimeDeposit(1, "premium", BALANCE, 100);

            calculator.updateBalance(List.of(deposit));
            calculator.updateBalance(List.of(deposit));

            // month 1: 10000.00 + 41.67 = 10041.67; month 2: 10041.67 + 41.84 = 10083.51
            assertThat(deposit.getBalance()).isEqualTo(10_083.51);
            assertThat(deposit.getDays()).isEqualTo(100); // days are not advanced
        }
    }
}
