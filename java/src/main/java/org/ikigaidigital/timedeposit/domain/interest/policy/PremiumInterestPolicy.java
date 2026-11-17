package org.ikigaidigital.timedeposit.domain.interest.policy;

import org.ikigaidigital.timedeposit.domain.TimeDeposit;
import org.springframework.stereotype.Component;

@Component
public class PremiumInterestPolicy extends AbstractInterestPolicy {

	public static final String PLAN_TYPE = "premium";
	private static final double ANNUAL_RATE = 0.05;
	private static final int DAYS_WITHOUT_INTEREST = 45;

	public PremiumInterestPolicy() {
		super(PLAN_TYPE, ANNUAL_RATE);
	}

	@Override
	protected boolean isEligible(TimeDeposit timeDeposit) {
		return timeDeposit.getDays() > DAYS_WITHOUT_INTEREST;
	}
}
