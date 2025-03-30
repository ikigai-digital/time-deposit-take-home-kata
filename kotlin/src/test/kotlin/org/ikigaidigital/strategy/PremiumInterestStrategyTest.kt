package org.ikigaidigital.strategy

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.strategy.PremiumInterestStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PremiumInterestStrategyTest {

    private lateinit var calculator: TimeDepositCalculator
    private lateinit var strategy: PremiumInterestStrategy

    @BeforeEach
    fun setUp() {
        calculator = mockk()
        strategy = PremiumInterestStrategy().apply {
            setCalculator(calculator)
        }
    }

    @Test
    fun `calculateInterest should use TimeDepositCalculator`() {
        val deposit = TimeDeposit(1, "premium", 1000.0, 90)
        every { calculator.updateBalance(listOf(deposit)) } answers {
            deposit.balance = 1100.0
        }

        val interest = strategy.calculateInterest(deposit)

        assertEquals(1100.0, interest)
        verify { calculator.updateBalance(listOf(deposit)) }
    }
}