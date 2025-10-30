package org.ikigaidigital.service.impl;

import org.ikigaidigital.model.TimeDeposit;
import org.ikigaidigital.service.InterestCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ikigaidigital.model.PlanType.BASIC;

class BasicInterestCalculatorTest {

    private final InterestCalculator testObj = new BasicInterestCalculator();

    @Test
    void calculateInterest() {

        TimeDeposit timeDeposit = new TimeDeposit(1, BASIC.name(), 1234567.00, 45);

        double actualInterest = testObj.calculate(timeDeposit);

        assertThat(actualInterest).isEqualTo(1028.8058333333333);
    }

}