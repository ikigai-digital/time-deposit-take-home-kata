package org.ikigaidigital.domain;

import org.ikigaidigital.TimeDeposit;

/**
 * Defines the plan-specific rule for one interest calculation.
 * Implementations decide eligibility and calculate interest; the calculator
 * selects the matching policy, rounds interest, and updates the deposit.
 */
public interface InterestPolicy {
    /**
     * Returns the exact plan name, such as "basic".
     * The calculator must match it case-sensitively to preserve existing behavior.
     */
    String getPlanType();

    /**
     * Calculates unrounded interest for a deposit of this policy's plan type.
     * Returns zero when the deposit is not eligible, without changing the deposit.
     * Keep the existing double arithmetic, including division by 12; rounding
     * and adding interest to the balance remain the calculator's responsibility.
     */
    double calculateInterest(TimeDeposit deposit);
}
