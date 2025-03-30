package org.ikigaidigital.strategy

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.strategy.StudentInterestStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StudentInterestStrategyTest {

    private lateinit var calculator: TimeDepositCalculator
    private lateinit var strategy: StudentInterestStrategy

    @BeforeEach
    fun setUp() {
        calculator = mockk()
        strategy = StudentInterestStrategy().apply {
            setCalculator(calculator)
        }
    }

    @Test
    fun `calculateInterest should use TimeDepositCalculator`() {
        val deposit = TimeDeposit(1, "student", 1000.0, 90)
        every { calculator.updateBalance(listOf(deposit)) } answers {
            deposit.balance = 1050.0
        }

        val interest = strategy.calculateInterest(deposit)

        assertEquals(1050.0, interest)
        verify { calculator.updateBalance(listOf(deposit)) }
    }
}