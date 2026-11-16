package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class InterestPolicyTest {

    private static final double BALANCE = 12000.00;
    private static final double PRECISION = 1e-9;

    static Stream<Arguments> interestCases() {
        return Stream.of(
                Arguments.of(new BasicInterestPolicy(), 30, 0.0),
                Arguments.of(new BasicInterestPolicy(), 31, 10.0),
                Arguments.of(new BasicInterestPolicy(), 1000, 10.0),
                Arguments.of(new StudentInterestPolicy(), 30, 0.0),
                Arguments.of(new StudentInterestPolicy(), 31, 30.0),
                Arguments.of(new StudentInterestPolicy(), 365, 30.0),
                Arguments.of(new StudentInterestPolicy(), 366, 0.0),
                Arguments.of(new PremiumInterestPolicy(), 30, 0.0),
                Arguments.of(new PremiumInterestPolicy(), 45, 0.0),
                Arguments.of(new PremiumInterestPolicy(), 46, 50.0),
                Arguments.of(new PremiumInterestPolicy(), 1000, 50.0));
    }

    @ParameterizedTest(name = "{0} after {1} days -> {2}")
    @MethodSource("interestCases")
    @DisplayName("Should calculate monthly interest of 12000.00 by plan rules")
    void shouldCalculateMonthlyInterestByPlanRules(InterestPolicy interestPolicy, int depositDays, double expectedInterest) {
        TimeDeposit timeDeposit = new TimeDeposit(1, interestPolicy.planType(), BALANCE, depositDays);

        double monthlyInterest = interestPolicy.calculateMonthlyInterest(timeDeposit);

        assertThat(monthlyInterest).isCloseTo(expectedInterest, within(PRECISION));
    }
}
