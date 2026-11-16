package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

public abstract class MonthlyRateInterestPolicy implements InterestPolicy {

    private static final int INTEREST_FREE_DAYS = 30;
    private static final int MONTHS_PER_YEAR = 12;

    private final String planType;
    private final double annualInterestRate;

    protected MonthlyRateInterestPolicy(String planType, double annualInterestRate) {
        this.planType = planType;
        this.annualInterestRate = annualInterestRate;
    }

    @Override
    public String planType() {
        return planType;
    }

    @Override
    public final double calculateMonthlyInterest(TimeDeposit timeDeposit) {
        int depositDays = timeDeposit.getDays();
        if (depositDays <= INTEREST_FREE_DAYS || !isInterestAccruedOn(depositDays)) {
            return 0;
        }
        return timeDeposit.getBalance() * annualInterestRate / MONTHS_PER_YEAR;
    }

    protected boolean isInterestAccruedOn(int depositDays) {
        return true;
    }
}
