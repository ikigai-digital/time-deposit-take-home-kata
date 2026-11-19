package org.ikigaidigital.application.port.outbound

import org.ikigaidigital.interest.InterestPolicy

fun interface InterestPlanRepository {
    fun findAll(): List<InterestPolicy>
}
