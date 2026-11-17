package org.ikigaidigital.timedeposit.domain.interest.policy;

import org.ikigaidigital.timedeposit.domain.TimeDeposit;

public interface InterestPolicy {

  double monthlyInterest(TimeDeposit timeDeposit);

  String planType();

}
