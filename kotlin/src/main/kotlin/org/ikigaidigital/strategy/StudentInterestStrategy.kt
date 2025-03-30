package org.ikigaidigital.strategy

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositCalculator

class StudentInterestStrategy : InterestStrategy {
    private lateinit var calculator: TimeDepositCalculator

    override fun calculateInterest(timeDeposit: TimeDeposit): Double {
        calculator.updateBalance(listOf(timeDeposit))
        return timeDeposit.balance
    }

    override fun setCalculator(calculator: TimeDepositCalculator) {
        this.calculator = calculator
    }
}