package org.ikigaidigital.timedeposit.domain.interest.calculator.policy;

import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
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
