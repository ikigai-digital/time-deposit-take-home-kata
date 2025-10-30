package org.ikigaidigital.service.impl;

import org.ikigaidigital.model.TimeDeposit;
import org.ikigaidigital.service.InterestCalculator;
import org.springframework.stereotype.Component;

@Component
public class BasicInterestCalculator implements InterestCalculator {

    @Override
    public double calculate(final TimeDeposit timeDeposit) {

        return timeDeposit.getBalance() * 0.01 / 12;

    }
}
