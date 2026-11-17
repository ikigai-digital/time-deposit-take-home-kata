package org.ikigaidigital.timedeposit.domain.interest.policy;


import org.ikigaidigital.timedeposit.domain.TimeDeposit;

public abstract class AbstractInterestPolicy implements InterestPolicy {

	private static final int MONTHS_PER_YEAR = 12;
	private static final double NO_INTEREST = 0;

	private final String planType;
	private final double annualRate;

	protected AbstractInterestPolicy(String planType, double annualRate) {
		this.planType = planType;
		this.annualRate = annualRate;
	}

	@Override
	public String planType() {
		return planType;
	}

	@Override
	public final double monthlyInterest(TimeDeposit timeDeposit) {
		return isEligible(timeDeposit) ? timeDeposit.getBalance() * annualRate / MONTHS_PER_YEAR : NO_INTEREST;
	}

	protected abstract boolean isEligible(TimeDeposit timeDeposit);
}
