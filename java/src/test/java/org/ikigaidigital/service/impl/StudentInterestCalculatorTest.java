package org.ikigaidigital.service.impl;

import org.ikigaidigital.model.TimeDeposit;
import org.ikigaidigital.service.InterestCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ikigaidigital.model.PlanType.STUDENT;

class StudentInterestCalculatorTest {

    private final InterestCalculator testObj = new StudentInterestCalculator();

    @Test
    void calculateInterest() {

        TimeDeposit timeDeposit = new TimeDeposit(1, STUDENT.name(), 1234567.00, 120);

        double actualInterest = testObj.calculate(timeDeposit);

        assertThat(actualInterest).isEqualTo(3086.4175);
    }
}