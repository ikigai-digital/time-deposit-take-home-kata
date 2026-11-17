package org.ikigaidigital.timedeposit.domain.interest;

import java.math.BigDecimal;
import org.ikigaidigital.timedeposit.domain.TimeDeposit;

public interface InterestCalculator {

  BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit);
}
