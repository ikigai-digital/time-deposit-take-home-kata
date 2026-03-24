package org.ikigaidigital.domain.service;

import org.ikigaidigital.TimeDeposit;

public class FallBackPlanStrategy implements InterestCalculationStrategy {
    @Override
    public double calculateMonthlyInterest(TimeDeposit deposit) {
        return 0;
    }

    @Override
    public String getPlanType() {
        return null;
    }
}
