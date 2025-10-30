package org.ikigaidigital.service;

import lombok.AllArgsConstructor;
import org.ikigaidigital.model.TimeDeposit;
import org.ikigaidigital.service.impl.BasicInterestCalculator;
import org.ikigaidigital.service.impl.PremiumInterestCalculator;
import org.ikigaidigital.service.impl.StudentInterestCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.ikigaidigital.model.PlanType.BASIC;
import static org.ikigaidigital.model.PlanType.PREMIUM;
import static org.ikigaidigital.model.PlanType.STUDENT;

@Component
@AllArgsConstructor
public class TimeDepositCalculator {

    private static final int NUMBER_OF_DAYS_NO_INTEREST_APPLIED = 30;

    private StudentInterestCalculator studentInterestCalculator;
    private PremiumInterestCalculator premiumInterestCalculator;
    private BasicInterestCalculator basicInterestCalculator;

    public void updateBalance(List<TimeDeposit> timeDeposits) {

        for (int i = 0; i < timeDeposits.size(); i++) {

            double interest = 0;
            TimeDeposit timeDeposit = timeDeposits.get(i);

            if (timeDeposit.getDays() > NUMBER_OF_DAYS_NO_INTEREST_APPLIED) {

                if (timeDeposit.getPlanType().equals(STUDENT.name())) {
                    interest += studentInterestCalculator.calculate(timeDeposit);
                } else if (timeDeposit.getPlanType().equals(PREMIUM.name())) {
                    interest += premiumInterestCalculator.calculate(timeDeposit);
                } else if (timeDeposits.get(i).getPlanType().equals(BASIC.name())) {
                    interest += basicInterestCalculator.calculate(timeDeposit) * 0.01 / 12;
                }
            }

            double balanceWithInterest
                    = timeDeposits.get(i).getBalance() +
                    (new BigDecimal(interest).setScale(2, RoundingMode.HALF_UP)).doubleValue();

            timeDeposits.get(i).setBalance(balanceWithInterest);
        }
    }

}
