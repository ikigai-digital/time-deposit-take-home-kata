package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDeposit;

public interface InterestCalculator {

    double calculate(final TimeDeposit timeDeposit);
}
