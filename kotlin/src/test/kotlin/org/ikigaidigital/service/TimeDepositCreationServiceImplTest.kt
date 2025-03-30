package org.ikigaidigital.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.persistence.entity.TimeDepositEntity
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class TimeDepositCreationServiceImplTest {

    private lateinit var timeDepositRepository: TimeDepositRepository
    private lateinit var timeDepositCreationService: TimeDepositCreationServiceImpl

    @BeforeEach
    fun setUp() {
        timeDepositRepository = mock(TimeDepositRepository::class.java)
        timeDepositCreationService = TimeDepositCreationServiceImpl(timeDepositRepository)
    }

    @Test
    fun `createTimeDeposit should create and return TimeDeposit`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(1000.0))
        `when`(timeDepositRepository.save(any(TimeDepositEntity::class.java))).thenReturn(entity)

        val result = timeDepositCreationService.createTimeDeposit(1000.0, 12, "basic", LocalDate.now())

        assertNotNull(result)
        assertEquals("basic", result.planType)
        assertEquals(1000.0, result.balance)
        assertEquals(360, result.days)
    }
}