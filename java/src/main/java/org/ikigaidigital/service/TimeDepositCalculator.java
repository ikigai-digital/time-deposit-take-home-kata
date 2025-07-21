package org.ikigaidigital.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.ikigaidigital.entity.PlanType;
import org.ikigaidigital.model.TimeDeposit;
import org.springframework.stereotype.Component;

@Component
public class TimeDepositCalculator {

    public void updateBalance(List<TimeDeposit> xs) {
        for (TimeDeposit x : xs) {
            double interest =
                InterestCalculatorFactory.getCalculator(PlanType.valueOf(x.getPlanType().toUpperCase())).calculateInterest(x);

            double a2d = x.getBalance() + (BigDecimal.valueOf(interest).setScale(2, RoundingMode.HALF_UP)).doubleValue();
            x.setBalance(a2d);
        }
    }
}
