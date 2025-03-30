package org.ikigaidigital.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.exception.TimeDepositNotFoundException
import org.ikigaidigital.persistence.entity.TimeDepositEntity
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.*

class TimeDepositRetrievalServiceImplTest {

    private lateinit var timeDepositRepository: TimeDepositRepository
    private lateinit var timeDepositRetrievalService: TimeDepositRetrievalServiceImpl

    @BeforeEach
    fun setUp() {
        timeDepositRepository = mock(TimeDepositRepository::class.java)
        timeDepositRetrievalService = TimeDepositRetrievalServiceImpl(timeDepositRepository)
    }

    @Test
    fun `getTimeDeposit should return TimeDeposit`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(1000.0))
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.of(entity))

        val result = timeDepositRetrievalService.getTimeDeposit(1)

        assertNotNull(result)
        assertEquals("basic", result.planType)
        assertEquals(1000.0, result.balance)
        assertEquals(360, result.days)
    }

    @Test
    fun `getTimeDeposit should throw TimeDepositNotFoundException`() {
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows(TimeDepositNotFoundException::class.java) {
            timeDepositRetrievalService.getTimeDeposit(1)
        }

        assertEquals("Time deposit with ID 1 not found", exception.message)
    }

    @Test
    fun `findAll should return list of TimeDeposits`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(1000.0))
        `when`(timeDepositRepository.findAll()).thenReturn(listOf(entity))

        val result = timeDepositRetrievalService.findAll()

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("basic", result[0].planType)
        assertEquals(1000.0, result[0].balance)
        assertEquals(360, result[0].days)
    }
}