package org.ikigaidigital.timedeposit.domain.interest.calculator.policy;

import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;

public interface InterestPolicy {

  double monthlyInterest(TimeDeposit timeDeposit);

  String planType();

}
