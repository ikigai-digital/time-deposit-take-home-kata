package org.ikigaidigital.timedeposit.domain.interest.policy;

import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
import org.springframework.stereotype.Component;

@Component
public class StudentInterestPolicy extends AbstractInterestPolicy {

	public static final String PLAN_TYPE = "student";
	private static final double ANNUAL_RATE = 0.03;
	private static final int FIRST_DAY_WITHOUT_INTEREST = 366;

	public StudentInterestPolicy() {
		super(PLAN_TYPE, ANNUAL_RATE);
	}

	@Override
	protected boolean isEligible(TimeDeposit timeDeposit) {
		return timeDeposit.getDays() < FIRST_DAY_WITHOUT_INTEREST;
	}
}
