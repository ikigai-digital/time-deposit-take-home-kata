package org.ikigaidigital.domain.interest;

public class PremiumInterestPolicy extends MonthlyRateInterestPolicy {

    public static final String PLAN_TYPE = "premium";

    private static final double ANNUAL_INTEREST_RATE = 0.05;
    private static final int LAST_INTEREST_FREE_DAY = 45;

    public PremiumInterestPolicy() {
        super(PLAN_TYPE, ANNUAL_INTEREST_RATE);
    }

    @Override
    protected boolean isInterestAccruedOn(int depositDays) {
        return depositDays > LAST_INTEREST_FREE_DAY;
    }
}
