package org.ikigaidigital.domain.interest;

public class BasicInterestPolicy extends MonthlyRateInterestPolicy {

    public static final String PLAN_TYPE = "basic";

    private static final double ANNUAL_INTEREST_RATE = 0.01;

    public BasicInterestPolicy() {
        super(PLAN_TYPE, ANNUAL_INTEREST_RATE);
    }
}
