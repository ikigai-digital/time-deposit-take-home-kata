package org.ikigaidigital.service.impl;

import org.ikigaidigital.model.TimeDeposit;
import org.ikigaidigital.service.InterestCalculator;
import org.springframework.stereotype.Component;

@Component
public class PremiumInterestCalculator implements InterestCalculator {

    private static final int NO_DAYS_45 = 45;

    @Override
    public double calculate(final TimeDeposit timeDeposit) {
        double interest = 0;

        if (timeDeposit.getDays() > NO_DAYS_45) {
            interest += timeDeposit.getBalance() * 0.05 / 12;
        }

        return interest;
    }
}
