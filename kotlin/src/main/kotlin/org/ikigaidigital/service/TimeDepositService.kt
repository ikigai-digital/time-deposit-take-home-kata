package org.ikigaidigital.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.exception.InsufficientBalanceException
import org.ikigaidigital.exception.TimeDepositNotFoundException
import java.math.BigDecimal
import java.time.LocalDate

interface TimeDepositCreationService {
    fun createTimeDeposit(
        amount: Double,
        termInMonths: Int,
        planType: String,
        startDate: LocalDate = LocalDate.now()
    ): TimeDeposit
}

interface TimeDepositCalculationService {
    fun calculateMaturityAmount(deposit: TimeDeposit): BigDecimal
    fun calculateMaturityAmountById(depositId: Int): BigDecimal
}

interface TimeDepositWithdrawalService {
    @Throws(TimeDepositNotFoundException::class, InsufficientBalanceException::class)
    fun withdrawTimeDeposit(depositId: Int, amount: Double, withdrawalDate: LocalDate = LocalDate.now()): BigDecimal
}

interface TimeDepositRetrievalService {
    @Throws(TimeDepositNotFoundException::class)
    fun getTimeDeposit(depositId: Int): TimeDeposit
    fun findAll(): List<TimeDeposit>
}

interface TimeDepositUpdateService {
    fun updateAllBalances()
}