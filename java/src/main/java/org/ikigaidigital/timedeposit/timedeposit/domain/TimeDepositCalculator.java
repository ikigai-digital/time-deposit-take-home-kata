package org.ikigaidigital.timedeposit.timedeposit.domain;

import java.util.List;
import org.ikigaidigital.timedeposit.timedeposit.domain.interest.InterestCalculator;

public class TimeDepositCalculator {

  private final InterestCalculator interestCalculator;

  public TimeDepositCalculator(InterestCalculator interestCalculator) {
    this.interestCalculator = interestCalculator;
  }

  public void updateBalance(List<TimeDeposit> xs) {
    xs.forEach(timeDeposit -> timeDeposit.setBalance(
        timeDeposit.getBalance() + interestCalculator.calculateMonthlyInterest(timeDeposit)
            .doubleValue()));
  }
}
