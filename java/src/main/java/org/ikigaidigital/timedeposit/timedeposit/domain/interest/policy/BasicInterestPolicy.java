package org.ikigaidigital.timedeposit.timedeposit.domain.interest.policy;

import org.ikigaidigital.timedeposit.timedeposit.domain.TimeDeposit;
import org.springframework.stereotype.Component;

@Component
public class BasicInterestPolicy extends AbstractInterestPolicy {

	public static final String PLAN_TYPE = "basic";
	private static final double ANNUAL_RATE = 0.01;

	public BasicInterestPolicy() {
		super(PLAN_TYPE, ANNUAL_RATE);
	}

	@Override
	protected boolean isEligible(TimeDeposit timeDeposit) {
		return true;
	}
}
