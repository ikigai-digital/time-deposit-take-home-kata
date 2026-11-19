package org.ikigaidigital.interest

import org.ikigaidigital.TimeDeposit

class MonthlyRateInterestPolicy(
    override val planType: String,
    private val annualRate: Double,
    private val interestStartsAfterDays: Int,
    private val interestEndsAfterDays: Int?,
) : InterestPolicy {

    override fun monthlyInterest(deposit: TimeDeposit): Double =
        if (isEligible(deposit.days)) deposit.balance * annualRate / MONTHS_PER_YEAR else 0.0

    private fun isEligible(days: Int): Boolean =
        days > interestStartsAfterDays && (interestEndsAfterDays == null || days <= interestEndsAfterDays)

    private companion object {
        const val MONTHS_PER_YEAR = 12
    }
}
