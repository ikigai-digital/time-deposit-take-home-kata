package org.ikigaidigital.domain;

import org.ikigaidigital.TimeDeposit;

/** Calculates unrounded student-plan interest without changing the deposit. */
public class StudentInterestPolicy implements InterestPolicy {
    @Override
    public String getPlanType() {
        // Keep the exact lowercase name used by the existing calculator.
        return "student";
    }

    @Override
    public double calculateInterest(TimeDeposit deposit) {
        // Student interest applies only on days 31 through 365, inclusive.
        if (deposit.getDays() <= 30 || deposit.getDays() >= 366) {
            return 0;
        }

        // Preserve the existing arithmetic. The calculator will round and add this amount.
        return deposit.getBalance() * 0.03 / 12;
    }
}
