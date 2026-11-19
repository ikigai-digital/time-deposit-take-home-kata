package org.ikigaidigital

import org.ikigaidigital.interest.InterestPolicy
import java.math.BigDecimal
import java.math.RoundingMode

class TimeDepositCalculator(policies: List<InterestPolicy>) {

    private val policiesByPlanType: Map<String, InterestPolicy> = policies.associateBy { it.planType }

    init {
        require(policiesByPlanType.size == policies.size) { "Duplicate interest policy for a plan type" }
    }

    fun updateBalance(xs: List<TimeDeposit>) {
        xs.forEach { deposit -> deposit.balance += monthlyInterest(deposit).toDouble() }
    }

    fun monthlyInterest(deposit: TimeDeposit): BigDecimal =
        BigDecimal(policiesByPlanType[deposit.planType]?.monthlyInterest(deposit) ?: 0.0)
            .setScale(2, RoundingMode.HALF_UP)
}
