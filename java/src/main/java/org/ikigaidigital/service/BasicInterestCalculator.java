package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDeposit;

public class BasicInterestCalculator implements InterestCalculator {

    @Override
    public double calculateInterest(TimeDeposit deposit) {
        if (deposit.getDays() > 30) {
            return deposit.getBalance() * 0.01 / 12;
        }
        return 0;
    }
}
