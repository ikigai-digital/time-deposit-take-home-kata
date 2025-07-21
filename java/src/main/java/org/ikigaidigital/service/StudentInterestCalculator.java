package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDeposit;

public class StudentInterestCalculator implements InterestCalculator {

    @Override
    public double calculateInterest(TimeDeposit deposit) {
        if (deposit.getDays() > 30 && deposit.getDays() < 366) {
            return deposit.getBalance() * 0.03 / 12;
        }
        return 0;
    }
}
