package org.ikigaidigital.adapter.outbound.persistence

import org.ikigaidigital.application.port.outbound.InterestPlanRepository
import org.ikigaidigital.interest.InterestPolicy
import org.ikigaidigital.interest.MonthlyRateInterestPolicy
import org.springframework.stereotype.Component

@Component
class InterestPlanPersistenceAdapter(
    private val plans: InterestPlanJpaRepository,
) : InterestPlanRepository {

    override fun findAll(): List<InterestPolicy> =
        plans.findAll().map {
            MonthlyRateInterestPolicy(it.planType, it.annualRate.toDouble(), it.interestStartsAfterDays, it.interestEndsAfterDays)
        }
}
