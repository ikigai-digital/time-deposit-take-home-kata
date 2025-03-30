package org.ikigaidigital.service

import org.ikigaidigital.exception.InsufficientBalanceException
import org.ikigaidigital.exception.TimeDepositNotFoundException
import org.ikigaidigital.persistence.entity.TimeDepositEntity
import org.ikigaidigital.persistence.entity.WithdrawalEntity
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.ikigaidigital.persistence.repository.WithdrawalRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*

class TimeDepositWithdrawalServiceImplTest {

    private lateinit var timeDepositRepository: TimeDepositRepository
    private lateinit var withdrawalRepository: WithdrawalRepository
    private lateinit var timeDepositWithdrawalService: TimeDepositWithdrawalServiceImpl

    @BeforeEach
    fun setUp() {
        timeDepositRepository = mock(TimeDepositRepository::class.java)
        withdrawalRepository = mock(WithdrawalRepository::class.java)
        timeDepositWithdrawalService = TimeDepositWithdrawalServiceImpl(timeDepositRepository, withdrawalRepository)
    }

    @Test
    fun `withdrawTimeDeposit should return remaining balance`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(1000.0))
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.of(entity))

        val result = timeDepositWithdrawalService.withdrawTimeDeposit(1, 200.0, LocalDate.now())

        assertEquals(BigDecimal(800.0).setScale(2, RoundingMode.HALF_UP), result)
        verify(withdrawalRepository).save(any(WithdrawalEntity::class.java))
    }

    @Test
    fun `withdrawTimeDeposit should throw InsufficientBalanceException`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(100.0))
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.of(entity))

        val exception = assertThrows(InsufficientBalanceException::class.java) {
            timeDepositWithdrawalService.withdrawTimeDeposit(1, 200.0, LocalDate.now())
        }

        assertEquals("Insufficient balance for withdrawal", exception.message)
    }

    @Test
    fun `withdrawTimeDeposit should throw TimeDepositNotFoundException`() {
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows(TimeDepositNotFoundException::class.java) {
            timeDepositWithdrawalService.withdrawTimeDeposit(1, 200.0, LocalDate.now())
        }

        assertEquals("Time deposit with ID 1 not found", exception.message)
    }

    @Test
    fun `withdrawTimeDeposit should handle zero balance`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal.ZERO)
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.of(entity))

        val exception = assertThrows(InsufficientBalanceException::class.java) {
            timeDepositWithdrawalService.withdrawTimeDeposit(1, 200.0, LocalDate.now())
        }

        assertEquals("Insufficient balance for withdrawal", exception.message)
    }

    @Test
    fun `withdrawTimeDeposit should handle zero withdrawal amount`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(1000.0))
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.of(entity))

        val result = timeDepositWithdrawalService.withdrawTimeDeposit(1, 0.0, LocalDate.now())

        assertEquals(BigDecimal(1000.0).setScale(2, RoundingMode.HALF_UP), result)
        verify(withdrawalRepository).save(any(WithdrawalEntity::class.java))
    }

    @Test
    fun `withdrawTimeDeposit should handle exact balance withdrawal`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(200.0))
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.of(entity))

        val result = timeDepositWithdrawalService.withdrawTimeDeposit(1, 200.0, LocalDate.now())

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result)
        verify(withdrawalRepository).save(any(WithdrawalEntity::class.java))
    }

    @Test
    fun `withdrawTimeDeposit should handle negative withdrawal amount`() {
        val entity = TimeDepositEntity(planType = "basic", days = 360, balance = BigDecimal(1000.0))
        `when`(timeDepositRepository.findById(1)).thenReturn(Optional.of(entity))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            timeDepositWithdrawalService.withdrawTimeDeposit(1, -200.0, LocalDate.now())
        }

        assertEquals("Withdrawal amount must be positive", exception.message)
    }
}