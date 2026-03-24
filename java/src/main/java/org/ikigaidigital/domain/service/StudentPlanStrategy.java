package org.ikigaidigital.domain.service;

import org.ikigaidigital.TimeDeposit;

public class StudentPlanStrategy implements InterestCalculationStrategy {
    private static final String PLAN_TYPE = "student";

    @Override
    public double calculateMonthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() <= 30 || deposit.getDays() >= 366) {
          return 0;
        }
        return deposit.getBalance() * 0.03 / 12;
    }

    @Override
    public String getPlanType() {
        return PLAN_TYPE;
    }
}
