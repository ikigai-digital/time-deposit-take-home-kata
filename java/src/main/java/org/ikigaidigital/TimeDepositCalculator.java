package org.ikigaidigital;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TimeDepositCalculator {
    private static final int MONTHS_PER_YEAR = 12;
    private static final int SCALE = 2;

    public void updateBalance(List<TimeDeposit> deposits) {
        for (TimeDeposit deposit : deposits) {
            double interest = monthlyInterest(deposit);
            double roundedInterest = roundToCents(interest);
            double newBalance = deposit.getBalance() + roundedInterest;
            deposit.setBalance(newBalance);
        }
    }

    private double monthlyInterest(TimeDeposit deposit) {
        // Checked before resolving the plan: the legacy code never read planType inside the
        // interest-free period, so e.g. a null planType there must not fail.
        if (PlanType.noPlanEarnsInterestAt(deposit.getDays())) {
            return 0;
        }
        return PlanType.fromCode(deposit.getPlanType())
                .filter(plan -> plan.earnsInterest(deposit.getDays()))
                .map(plan -> deposit.getBalance() * plan.annualInterestRate() / MONTHS_PER_YEAR)
                .orElse(0.0);
    }

    private double roundToCents(double amount) {
        return new BigDecimal(amount).setScale(SCALE, RoundingMode.HALF_UP).doubleValue();
    }
}
