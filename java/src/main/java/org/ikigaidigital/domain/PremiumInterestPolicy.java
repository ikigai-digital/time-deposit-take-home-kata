package org.ikigaidigital.domain;

import org.ikigaidigital.TimeDeposit;

/** Calculates unrounded premium-plan interest without changing the deposit. */
public class PremiumInterestPolicy implements InterestPolicy {
    @Override
    public String getPlanType() {
        // Keep the exact lowercase name used by the existing calculator.
        return "premium";
    }

    @Override
    public double calculateInterest(TimeDeposit deposit) {
        // Premium's waiting period includes the common first 30 days and extends through day 45.
        if (deposit.getDays() <= 45) {
            return 0;
        }

        // Preserve the existing arithmetic. The calculator will round and add this amount.
        return deposit.getBalance() * 0.05 / 12;
    }
}
