package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

public interface InterestPolicy {

    String planType();

    double calculateMonthlyInterest(TimeDeposit timeDeposit);
}
