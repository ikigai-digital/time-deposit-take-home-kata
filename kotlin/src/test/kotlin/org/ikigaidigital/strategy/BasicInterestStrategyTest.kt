package org.ikigaidigital.strategy

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.strategy.BasicInterestStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BasicInterestStrategyTest {

    private lateinit var calculator: TimeDepositCalculator
    private lateinit var strategy: BasicInterestStrategy

    @BeforeEach
    fun setUp() {
        calculator = mockk()
        strategy = BasicInterestStrategy().apply {
            setCalculator(calculator)
        }
    }

    @Test
    fun `calculateInterest should use TimeDepositCalculator`() {
        val deposit = TimeDeposit(1, "basic", 1000.0, 90)
        every { calculator.updateBalance(listOf(deposit)) } answers {
            deposit.balance = 1025.0
        }

        val interest = strategy.calculateInterest(deposit)

        assertEquals(1025.0, interest)
        verify { calculator.updateBalance(listOf(deposit)) }
    }
}