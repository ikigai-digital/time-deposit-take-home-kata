package org.ikigaidigital.domain.service;

import org.ikigaidigital.TimeDeposit;

public interface InterestCalculationStrategy {
    double calculateMonthlyInterest(TimeDeposit deposit);
    String getPlanType();
}
