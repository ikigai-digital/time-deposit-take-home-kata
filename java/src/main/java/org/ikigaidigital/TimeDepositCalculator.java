package org.ikigaidigital;

import org.ikigaidigital.domain.BasicInterestPolicy;
import org.ikigaidigital.domain.InterestPolicy;
import org.ikigaidigital.domain.PremiumInterestPolicy;
import org.ikigaidigital.domain.StudentInterestPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TimeDepositCalculator {
    private final List<InterestPolicy> policies;

    public TimeDepositCalculator() {
        // Preserve no-argument construction with the three standard plan rules.
        this(List.of(new BasicInterestPolicy(), new StudentInterestPolicy(), new PremiumInterestPolicy()));
    }

    public TimeDepositCalculator(List<InterestPolicy> policies) {
        // Snapshot the supplied rules so later list changes do not alter this calculator.
        this.policies = List.copyOf(policies);
    }

    public void updateBalance(List<TimeDeposit> deposits) {
        for (TimeDeposit deposit : deposits) {
            // Unknown plans retain zero interest; matching remains case-sensitive.
            double monthlyInterest = 0;
            for (InterestPolicy policy : policies) {
                if (deposit.getPlanType().equals(policy.getPlanType())) {
                    monthlyInterest += policy.calculateInterest(deposit);
                    break;
                }
            }

            // Preserve BigDecimal(double) and HALF_UP: changing the conversion changes edge cases.
            double roundedInterest = new BigDecimal(monthlyInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
            // Round only interest, then update the original object without advancing its days.
            double updatedBalance = deposit.getBalance() + roundedInterest;
            deposit.setBalance(updatedBalance);
        }
    }
}
