package org.ikigaidigital.domain.service;

import org.ikigaidigital.TimeDeposit;

public class BasicPlanStrategy implements InterestCalculationStrategy {
    private static final String PLAN_TYPE = "basic";

    @Override
    public double calculateMonthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() <= 30) {
          return 0;
        }
        return deposit.getBalance() * 0.01 / 12;
    }

    @Override
    public String getPlanType() {
        return PLAN_TYPE;
    }
}
