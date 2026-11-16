package org.ikigaidigital;

import org.ikigaidigital.domain.interest.BasicInterestPolicy;
import org.ikigaidigital.domain.interest.InterestPolicy;
import org.ikigaidigital.domain.interest.PremiumInterestPolicy;
import org.ikigaidigital.domain.interest.StudentInterestPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimeDepositCalculator {

    private static final int INTEREST_SCALE = 2;
    private static final double NO_INTEREST = 0;

    private final Map<String, InterestPolicy> interestPoliciesByPlanType;

    public TimeDepositCalculator() {
        this(List.of(new BasicInterestPolicy(), new StudentInterestPolicy(), new PremiumInterestPolicy()));
    }

    public TimeDepositCalculator(List<InterestPolicy> interestPolicies) {
        this.interestPoliciesByPlanType = interestPolicies.stream()
                .collect(Collectors.toUnmodifiableMap(InterestPolicy::planType, Function.identity()));
    }

    public void updateBalance(List<TimeDeposit> timeDeposits) {
        for (TimeDeposit timeDeposit : timeDeposits) {
            double roundedInterest = roundInterest(calculateMonthlyInterest(timeDeposit));
            timeDeposit.setBalance(timeDeposit.getBalance() + roundedInterest);
        }
    }

    private double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        InterestPolicy interestPolicy = interestPoliciesByPlanType.get(timeDeposit.getPlanType());
        if (interestPolicy == null) {
            return NO_INTEREST;
        }
        return interestPolicy.calculateMonthlyInterest(timeDeposit);
    }

    private double roundInterest(double interest) {
        return new BigDecimal(interest).setScale(INTEREST_SCALE, RoundingMode.HALF_UP).doubleValue();
    }
}
