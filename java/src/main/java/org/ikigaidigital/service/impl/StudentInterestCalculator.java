package org.ikigaidigital.service.impl;

import org.ikigaidigital.model.TimeDeposit;
import org.ikigaidigital.service.InterestCalculator;
import org.springframework.stereotype.Component;

@Component
public class StudentInterestCalculator implements InterestCalculator {

    private static final int DAYS_IN_YEAR = 366;

    @Override
    public double calculate(final TimeDeposit timeDeposit) {
        double interest = 0;

        if (timeDeposit.getDays() < DAYS_IN_YEAR) {
            interest += timeDeposit.getBalance() * 0.03 / 12;
        }

        return interest;
    }
}
