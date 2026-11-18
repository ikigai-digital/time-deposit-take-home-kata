package org.ikigaidigital.timedeposit.domain;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ikigaidigital.timedeposit.domain.interest.InterestCalculator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeDepositCalculator {

  private final InterestCalculator interestCalculator;

  public void updateBalance(List<TimeDeposit> xs) {
    xs.forEach(timeDeposit -> timeDeposit.setBalance(
        timeDeposit.getBalance() + interestCalculator.calculateMonthlyInterest(timeDeposit)
            .doubleValue()));
  }
}
