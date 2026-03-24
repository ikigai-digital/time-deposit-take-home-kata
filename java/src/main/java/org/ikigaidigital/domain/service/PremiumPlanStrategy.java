package org.ikigaidigital.domain.service;

import org.ikigaidigital.TimeDeposit;

public class PremiumPlanStrategy implements InterestCalculationStrategy {
    private static final String PLAN_TYPE = "premium";

    @Override
    public double calculateMonthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() <= 45) {
          return 0;
        }
        return deposit.getBalance() * 0.05 / 12;
    }

    @Override
    public String getPlanType() {
        return PLAN_TYPE;
    }
}
