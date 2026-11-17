package org.ikigaidigital.timedeposit.timedeposit.domain.interest.policy;

import org.ikigaidigital.timedeposit.timedeposit.domain.TimeDeposit;

public interface InterestPolicy {

  double monthlyInterest(TimeDeposit timeDeposit);

  String planType();

}
