package org.ikigaidigital.domain.interest;

public class StudentInterestPolicy extends MonthlyRateInterestPolicy {

    public static final String PLAN_TYPE = "student";

    private static final double ANNUAL_INTEREST_RATE = 0.03;
    private static final int LAST_INTEREST_DAY = 365;

    public StudentInterestPolicy() {
        super(PLAN_TYPE, ANNUAL_INTEREST_RATE);
    }

    @Override
    protected boolean isInterestAccruedOn(int depositDays) {
        return depositDays <= LAST_INTEREST_DAY;
    }
}
