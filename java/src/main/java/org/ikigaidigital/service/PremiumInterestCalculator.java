package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDeposit;

public class PremiumInterestCalculator implements InterestCalculator {

    @Override
    public double calculateInterest(TimeDeposit deposit) {
        if (deposit.getDays() > 30 && deposit.getDays() > 45) {
            return deposit.getBalance() * 0.05 / 12;
        }
        return 0;
    }
}
