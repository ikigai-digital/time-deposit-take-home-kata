package org.ikigaidigital.adapter.outbound.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "interestPlans")
class InterestPlanEntity(
    @Id
    val planType: String,

    @Column(nullable = false, precision = 7, scale = 4)
    val annualRate: BigDecimal,

    @Column(nullable = false)
    val interestStartsAfterDays: Int,

    val interestEndsAfterDays: Int?,
)
