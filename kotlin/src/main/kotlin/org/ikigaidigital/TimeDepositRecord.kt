package org.ikigaidigital

import java.math.BigDecimal
import java.time.YearMonth

data class TimeDepositRecord(
    val id: Int,
    val planType: String,
    val days: Int,
    val balance: BigDecimal,
    val lastInterestMonth: YearMonth?,
) {
    fun withMonthlyInterest(calculator: TimeDepositCalculator, month: YearMonth): TimeDepositRecord =
        copy(balance = balance + calculator.monthlyInterest(toTimeDeposit()), lastInterestMonth = month)

    private fun toTimeDeposit() = TimeDeposit(id, planType, balance.toDouble(), days)
}
