package org.ikigaidigital.strategy

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositCalculator

interface InterestStrategy {
    fun calculateInterest(timeDeposit: TimeDeposit): Double
    fun setCalculator(calculator: TimeDepositCalculator)
}