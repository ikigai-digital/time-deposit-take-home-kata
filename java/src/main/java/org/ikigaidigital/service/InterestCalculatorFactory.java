package org.ikigaidigital.service;

import org.ikigaidigital.entity.PlanType;

public class InterestCalculatorFactory {

    public static InterestCalculator getCalculator(PlanType planType) {
        return switch (planType) {
            case BASIC -> new BasicInterestCalculator();
            case STUDENT -> new StudentInterestCalculator();
            case PREMIUM -> new PremiumInterestCalculator();
            default -> throw new IllegalArgumentException("Unknown plan type: " + planType);
        };
    }
}

