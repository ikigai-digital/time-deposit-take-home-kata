package org.ikigaidigital;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.ikigaidigital.domain.service.InterestCalculationStrategy;
import org.ikigaidigital.domain.service.InterestCalculationStrategyFactory;

public class TimeDepositCalculator {
    private final InterestCalculationStrategyFactory strategyFactory;

    public TimeDepositCalculator(InterestCalculationStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public void updateBalance(List<TimeDeposit> timeDeposits) {
        timeDeposits.forEach(this::updateSingleBalance);
    }

    private void updateSingleBalance(TimeDeposit timeDeposit) {
        InterestCalculationStrategy strategy = strategyFactory.getStrategy(timeDeposit.getPlanType());
        double interest = strategy.calculateMonthlyInterest(timeDeposit);
        double roundedInterest = BigDecimal.valueOf(interest)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        timeDeposit.setBalance(timeDeposit.getBalance() + roundedInterest);
    }
}
