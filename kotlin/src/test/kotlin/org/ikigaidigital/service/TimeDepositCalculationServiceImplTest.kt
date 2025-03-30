package org.ikigaidigital.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.ikigaidigital.strategy.InterestStrategy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class TimeDepositCalculationServiceImplTest {

    private lateinit var timeDepositRepository: TimeDepositRepository
    private lateinit var timeDepositCalculationService: TimeDepositCalculationServiceImpl
    private lateinit var interestStrategies: Map<String, InterestStrategy>

    @BeforeEach
    fun setUp() {
        timeDepositRepository = mock(TimeDepositRepository::class.java)
        interestStrategies = mapOf(
            "basic" to mock(InterestStrategy::class.java),
            "student" to mock(InterestStrategy::class.java),
            "premium" to mock(InterestStrategy::class.java)
        )
        timeDepositCalculationService = TimeDepositCalculationServiceImpl(timeDepositRepository, interestStrategies)
    }

    @Test
    fun `calculateMaturityAmount should return correct amount`() {
        val timeDeposit = TimeDeposit(1, "basic", 1000.0, 360)
        `when`(interestStrategies["basic"]!!.calculateInterest(timeDeposit)).thenReturn(1000.0)

        val result = timeDepositCalculationService.calculateMaturityAmount(timeDeposit)

        assertEquals(BigDecimal(1000.0).setScale(2, RoundingMode.HALF_UP), result)
    }
}