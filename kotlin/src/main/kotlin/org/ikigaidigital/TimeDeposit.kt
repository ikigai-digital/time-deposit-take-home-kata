package org.ikigaidigital
import org.ikigaidigital.strategy.BasicInterestStrategy
import org.ikigaidigital.strategy.InterestStrategy
import org.ikigaidigital.strategy.PremiumInterestStrategy
import org.ikigaidigital.strategy.StudentInterestStrategy

data class TimeDeposit(
    val id: Int,
    val planType: String,
    var balance: Double,
    val days: Int
) {
    private val interestStrategy: InterestStrategy = when(planType.lowercase()) {
        "basic" -> BasicInterestStrategy()
        "student" -> StudentInterestStrategy()
        "premium" -> PremiumInterestStrategy()
        else -> throw IllegalArgumentException("Invalid plan type")
    }.apply {
        setCalculator(TimeDepositCalculator())
    }

    fun updateBalance() {
        balance = interestStrategy.calculateInterest(this)
    }
}
