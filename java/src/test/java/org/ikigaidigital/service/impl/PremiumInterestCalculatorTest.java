package org.ikigaidigital.service.impl;

import org.ikigaidigital.model.TimeDeposit;
import org.ikigaidigital.service.InterestCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ikigaidigital.model.PlanType.PREMIUM;

class PremiumInterestCalculatorTest {

    private final InterestCalculator testObj = new PremiumInterestCalculator();

    @Test
    void calculateInterest() {

        TimeDeposit timeDeposit = new TimeDeposit(1, PREMIUM.name(), 1234567.00, 250);

        double actualInterest = testObj.calculate(timeDeposit);

        assertThat(actualInterest).isEqualTo(5144.029166666667);
    }
}