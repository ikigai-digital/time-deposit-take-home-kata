package org.ikigaidigital.interest

import org.ikigaidigital.TimeDeposit

interface InterestPolicy {
    val planType: String

    fun monthlyInterest(deposit: TimeDeposit): Double
}
