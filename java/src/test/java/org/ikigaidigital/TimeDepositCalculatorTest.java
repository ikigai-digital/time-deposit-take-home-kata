package org.ikigaidigital;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeDepositCalculatorTest {
    @Test
    public void basicDepositEarnsNoInterestAtThirtyDays() {
        // Arrange: day 30 is still within the initial period without interest.
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1200.00, 30);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: apply the existing calculation to this deposit.
        calculator.updateBalance(List.of(deposit));

        // Assert: eligibility requires more than 30 days, so the balance stays unchanged.
        // This catches accidentally changing the boundary from days > 30 to days >= 30.
        assertThat(deposit.getBalance()).isEqualTo(1200.00);
    }

    @Test
    public void basicDepositEarnsInterestAfterThirtyDays() {
        // Arrange: day 31 is the first eligible day for the basic plan.
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1200.00, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: the existing method updates the supplied deposit in place.
        calculator.updateBalance(List.of(deposit));

        // Assert: preserve the existing formula, including division by 12.
        // Interest is 1200 * 0.01 / 12 = 1.00, so the new balance is 1201.00.
        // This characterization test captures current behavior before refactoring.
        assertThat(deposit.getBalance()).isEqualTo(1201.00);
    }

    @Test
    public void studentDepositEarnsNoInterestAtThirtyDays() {
        // Arrange: the initial 30-day period without interest also applies to student deposits.
        TimeDeposit deposit = new TimeDeposit(1, "student", 1200.00, 30);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: apply the existing calculation to this student deposit.
        calculator.updateBalance(List.of(deposit));

        // Assert: the student rate must not apply until after day 30.
        // This catches applying student interest too early, which would produce 1203.00.
        assertThat(deposit.getBalance()).isEqualTo(1200.00);
    }

    @Test
    public void studentDepositEarnsInterestAfterThirtyDays() {
        // Arrange: day 31 is past the initial waiting period and within the student plan's first year.
        TimeDeposit deposit = new TimeDeposit(1, "student", 1200.00, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: apply the existing calculation using the student plan's rate.
        calculator.updateBalance(List.of(deposit));

        // Assert: preserve the student formula, including division by 12.
        // Interest is 1200 * 0.03 / 12 = 3.00, so the new balance is 1203.00.
        assertThat(deposit.getBalance()).isEqualTo(1203.00);
    }

    @Test
    public void studentDepositEarnsInterestAtThreeHundredSixtyFiveDays() {
        // Arrange: day 365 is the last eligible day under the existing days < 366 rule.
        TimeDeposit deposit = new TimeDeposit(1, "student", 1200.00, 365);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: calculate interest on the last eligible student day.
        calculator.updateBalance(List.of(deposit));

        // Assert: 1200 * 0.03 / 12 = 3.00, so the balance becomes 1203.00.
        // This catches cutting off student interest one day too early.
        assertThat(deposit.getBalance()).isEqualTo(1203.00);
    }

    @Test
    public void studentDepositEarnsNoInterestAtThreeHundredSixtySixDays() {
        // Arrange: day 366 is the first day excluded by the student plan's upper limit.
        TimeDeposit deposit = new TimeDeposit(1, "student", 1200.00, 366);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: apply the calculation after student eligibility has ended.
        calculator.updateBalance(List.of(deposit));

        // Assert: no interest is added; this catches extending eligibility to day 366.
        assertThat(deposit.getBalance()).isEqualTo(1200.00);
    }

    @Test
    public void premiumDepositEarnsNoInterestAtThirtyDays() {
        // Arrange: premium deposits are also subject to the initial waiting period.
        TimeDeposit deposit = new TimeDeposit(1, "premium", 1200.00, 30);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: calculate at the end of the common initial waiting period.
        calculator.updateBalance(List.of(deposit));

        // Assert: premium interest must not start at day 30.
        assertThat(deposit.getBalance()).isEqualTo(1200.00);
    }

    @Test
    public void premiumDepositEarnsNoInterestAtFortyFiveDays() {
        // Arrange: premium has an additional waiting period through day 45.
        TimeDeposit deposit = new TimeDeposit(1, "premium", 1200.00, 45);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: calculate on the last day of the premium waiting period.
        calculator.updateBalance(List.of(deposit));

        // Assert: days > 45 excludes exactly day 45.
        assertThat(deposit.getBalance()).isEqualTo(1200.00);
    }

    @Test
    public void premiumDepositEarnsInterestAtFortySixDays() {
        // Arrange: day 46 is the first eligible day for premium interest.
        TimeDeposit deposit = new TimeDeposit(1, "premium", 1200.00, 46);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: apply the premium calculation on its first eligible day.
        calculator.updateBalance(List.of(deposit));

        // Assert: 1200 * 0.05 / 12 = 5.00, so the balance becomes 1205.00.
        assertThat(deposit.getBalance()).isEqualTo(1205.00);
    }

    @Test
    public void roundsInterestBelowHalfACentDown() {
        // Arrange: basic interest is about 0.00499167, below half a cent.
        TimeDeposit deposit = new TimeDeposit(1, "basic", 5.99, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: round the calculated interest using the existing implementation.
        calculator.updateBalance(List.of(deposit));

        // Assert: interest rounds to zero, leaving the balance unchanged.
        assertThat(deposit.getBalance()).isEqualTo(5.99);
    }

    @Test
    public void roundsInterestAboveHalfACentUp() {
        // Arrange: basic interest is about 0.00500833, above half a cent.
        TimeDeposit deposit = new TimeDeposit(1, "basic", 6.01, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: round and add the interest.
        calculator.updateBalance(List.of(deposit));

        // Assert: interest rounds to 0.01, producing 6.02.
        assertThat(deposit.getBalance()).isEqualTo(6.02);
    }

    @Test
    public void roundsExactlyRepresentableHalfCentInterestUp() {
        // Arrange: 150 * 0.01 / 12 = 0.125, an exactly representable binary fraction.
        TimeDeposit deposit = new TimeDeposit(1, "basic", 150.00, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: apply the existing HALF_UP rounding at an exact midpoint.
        calculator.updateBalance(List.of(deposit));

        // Assert: 0.125 rounds to 0.13, rather than the HALF_EVEN result 0.12.
        assertThat(deposit.getBalance()).isEqualTo(150.13);
    }

    @Test
    public void preservesBinaryFloatingPointRoundingNearHalfACent() {
        // Arrange: mathematically, 6 * 0.03 / 12 = 0.015.
        // The existing double calculation represents this value slightly below 0.015.
        TimeDeposit deposit = new TimeDeposit(1, "student", 6.00, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: preserve new BigDecimal(double), which keeps that binary approximation.
        calculator.updateBalance(List.of(deposit));

        // Assert: existing interest rounds to 0.01, not the decimal-arithmetic result 0.02.
        // This catches a seemingly harmless change to BigDecimal.valueOf or decimal-only arithmetic.
        assertThat(deposit.getBalance()).isEqualTo(6.01);
    }

    @Test
    public void roundsInterestWithoutRoundingTheFinalBalance() {
        // Arrange: the shared Double model allows a balance with more than two decimal places.
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1200.001, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: interest of about 1.00000083 rounds to 1.00 before being added.
        calculator.updateBalance(List.of(deposit));

        // Assert: the original fractional balance is retained; only interest is rounded.
        assertThat(deposit.getBalance()).isEqualTo(1201.001);
    }

    @Test
    public void zeroBalanceDepositsEarnNoInterest() {
        // Arrange: all three plans are eligible at day 46, but each balance is zero.
        List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 0.00, 46),
                new TimeDeposit(2, "student", 0.00, 46),
                new TimeDeposit(3, "premium", 0.00, 46));
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: apply all three interest rates.
        calculator.updateBalance(deposits);

        // Assert: zero times any supported rate remains zero.
        assertThat(deposits).extracting(TimeDeposit::getBalance)
                .containsExactly(0.00, 0.00, 0.00);
    }

    @Test
    public void unrecognizedPlanEarnsNoInterest() {
        // Arrange: a future plan name is not one of the three existing supported names.
        TimeDeposit deposit = new TimeDeposit(1, "future", 1200.00, 46);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: process an unrecognized plan after all existing waiting periods.
        calculator.updateBalance(List.of(deposit));

        // Assert: preserve the existing no-interest fallback, rather than a default plan rate.
        assertThat(deposit.getBalance()).isEqualTo(1200.00);
    }

    @Test
    public void differentlyCasedPlanNamesEarnNoInterest() {
        // Arrange: these names differ from the supported lowercase names only in capitalization.
        List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "Basic", 1200.00, 46),
                new TimeDeposit(2, "Student", 1200.00, 46),
                new TimeDeposit(3, "Premium", 1200.00, 46));
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: let the existing case-sensitive matching select a plan.
        calculator.updateBalance(deposits);

        // Assert: none match; normalizing case during refactoring would change these balances.
        assertThat(deposits).extracting(TimeDeposit::getBalance)
                .containsExactly(1200.00, 1200.00, 1200.00);
    }

    @Test
    public void emptyDepositListRemainsEmpty() {
        // Arrange: use a mutable empty list so an accidental insertion can be observed.
        List<TimeDeposit> deposits = new ArrayList<>();
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: an empty input should finish normally without any work.
        calculator.updateBalance(deposits);

        // Assert: no deposits were added.
        assertThat(deposits).isEmpty();
    }

    @Test
    public void mixedDepositListUpdatesEveryDepositUsingItsOwnRules() {
        // Arrange: interleave eligible, waiting, expired, and unknown plans.
        // The final eligible deposit catches accidentally stopping at an ineligible entry.
        List<TimeDeposit> deposits = List.of(
                new TimeDeposit(1, "basic", 1200.00, 31),
                new TimeDeposit(2, "student", 1200.00, 366),
                new TimeDeposit(3, "premium", 1200.00, 46),
                new TimeDeposit(4, "basic", 1200.00, 30),
                new TimeDeposit(5, "future", 1200.00, 46),
                new TimeDeposit(6, "student", 1200.00, 31));
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: update the whole list in one call.
        calculator.updateBalance(deposits);

        // Assert: eligibility and interest are determined independently for each entry.
        // This also catches reusing a previous deposit's interest.
        assertThat(deposits).extracting(TimeDeposit::getBalance)
                .containsExactly(1201.00, 1200.00, 1205.00, 1200.00, 1200.00, 1203.00);
    }

    @Test
    public void updatesBalancesInPlaceWithoutChangingOtherState() {
        // Arrange: keep references to two distinct deposits in a mutable list.
        TimeDeposit basic = new TimeDeposit(11, "basic", 1200.00, 31);
        TimeDeposit student = new TimeDeposit(22, "student", 1200.00, 365);
        List<TimeDeposit> deposits = new ArrayList<>(List.of(basic, student));
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act: update balances through the shared public API.
        calculator.updateBalance(deposits);

        // Assert: do not replace, remove, add, or reorder the supplied deposit objects.
        assertThat(deposits).hasSize(2);
        assertThat(deposits.get(0)).isSameAs(basic);
        assertThat(deposits.get(1)).isSameAs(student);

        // Assert: only balances change; identity fields, plan names, and ages are retained.
        assertThat(basic.getId()).isEqualTo(11);
        assertThat(basic.getPlanType()).isEqualTo("basic");
        assertThat(basic.getDays()).isEqualTo(31);
        assertThat(basic.getBalance()).isEqualTo(1201.00);
        assertThat(student.getId()).isEqualTo(22);
        assertThat(student.getPlanType()).isEqualTo("student");
        assertThat(student.getDays()).isEqualTo(365);
        assertThat(student.getBalance()).isEqualTo(1203.00);
    }

    @Test
    public void repeatedUpdatesCalculateInterestOnTheUpdatedBalance() {
        // Arrange: a large balance makes the second interest payment visibly different.
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1000000.00, 31);
        TimeDepositCalculator calculator = new TimeDepositCalculator();

        // Act and assert: the first rounded payment is 833.33.
        calculator.updateBalance(List.of(deposit));
        assertThat(deposit.getBalance()).isEqualTo(1000833.33);

        // Act again: there is no clock or one-payment guard in the existing method.
        calculator.updateBalance(List.of(deposit));

        // Assert: interest on 1000833.33 rounds to 834.03, producing 1001667.36.
        // Reusing the original balance or skipping the second payment would fail here.
        assertThat(deposit.getBalance()).isEqualTo(1001667.36);
    }
}
