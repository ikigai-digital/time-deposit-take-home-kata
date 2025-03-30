package org.ikigaidigital.service

import org.ikigaidigital.persistence.entity.TimeDepositEntity
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal

class TimeDepositUpdateServiceImplTest {

    private lateinit var timeDepositRepository: TimeDepositRepository
    private lateinit var timeDepositUpdateService: TimeDepositUpdateServiceImpl

    @BeforeEach
    fun setUp() {
        timeDepositRepository = mock(TimeDepositRepository::class.java)
        timeDepositUpdateService = TimeDepositUpdateServiceImpl(timeDepositRepository)
    }

    @Test
    fun `updateAllBalances should update balances`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(1000.0))
        `when`(timeDepositRepository.findAll()).thenReturn(listOf(entity))

        timeDepositUpdateService.updateAllBalances()

        verify(timeDepositRepository).saveAll(anyList())
    }
}