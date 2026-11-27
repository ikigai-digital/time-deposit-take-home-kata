package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PlanTypeTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "basic,   BASIC",
            "student, STUDENT",
            "premium, PREMIUM",
    })
    void resolvesKnownCodes(String code, PlanType expected) {
        assertThat(PlanType.fromCode(code)).contains(expected);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "BASIC,   0.01",
            "STUDENT, 0.03",
            "PREMIUM, 0.05",
    })
    void carriesAnnualInterestRateAsFraction(PlanType plan, double expectedRate) {
        assertThat(plan.annualInterestRate()).isEqualTo(expectedRate);
    }

    @ParameterizedTest(name = "{0} at {1} days -> {2}")
    @CsvSource({
            "BASIC,   30,   false",
            "BASIC,   31,   true",
            "BASIC,   5000, true",
            "STUDENT, 30,   false",
            "STUDENT, 31,   true",
            "STUDENT, 365,  true",
            "STUDENT, 366,  false",
            "PREMIUM, 45,   false",
            "PREMIUM, 46,   true",
            "PREMIUM, 5000, true",
    })
    void earnsInterestOnlyWithinItsDayWindow(PlanType plan, int days, boolean expected) {
        assertThat(plan.earnsInterest(days)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "days={0} -> {1}")
    @CsvSource({"0, true", "30, true", "31, false", "1000, false"})
    void interestFreePeriodIsDerivedFromEarliestPlanStart(int days, boolean expected) {
        assertThat(PlanType.noPlanEarnsInterestAt(days)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(PlanType.class)
    void codeRoundTrips(PlanType plan) {
        assertThat(PlanType.fromCode(plan.code())).contains(plan);
    }

    @ParameterizedTest(name = "\"{0}\"")
    @CsvSource({"Basic", "STUDENT", "Premium", "gold", "''", "' basic'"})
    void unknownOrWrongCaseCodesResolveToEmpty(String code) {
        assertThat(PlanType.fromCode(code)).isEmpty();
    }

    @Test
    void nullCodeIsRejected() {
        assertThatNullPointerException().isThrownBy(() -> PlanType.fromCode(null));
    }
}
