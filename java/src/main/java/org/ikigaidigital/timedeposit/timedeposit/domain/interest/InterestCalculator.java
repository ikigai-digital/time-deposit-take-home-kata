package org.ikigaidigital.timedeposit.timedeposit.domain.interest;

import java.math.BigDecimal;
import org.ikigaidigital.timedeposit.timedeposit.domain.TimeDeposit;

public interface InterestCalculator {

  BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit);
}
