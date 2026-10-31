package org.ikigaidigital.domain;

import org.ikigaidigital.TimeDeposit;

/** Calculates unrounded basic-plan interest without changing the deposit. */
public class BasicInterestPolicy implements InterestPolicy {
    @Override
    public String getPlanType() {
        // Keep the exact lowercase name used by the existing calculator.
        return "basic";
    }

    @Override
    public double calculateInterest(TimeDeposit deposit) {
        // The first 30 days earn no interest; day 31 is the first eligible day.
        if (deposit.getDays() <= 30) {
            return 0;
        }

        // Preserve the existing arithmetic. The calculator will round and add this amount.
        return deposit.getBalance() * 0.01 / 12;
    }
}
