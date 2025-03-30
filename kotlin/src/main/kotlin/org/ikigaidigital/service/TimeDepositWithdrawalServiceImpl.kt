package org.ikigaidigital.service

import org.ikigaidigital.exception.InsufficientBalanceException
import org.ikigaidigital.exception.TimeDepositNotFoundException
import org.ikigaidigital.persistence.entity.WithdrawalEntity
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.ikigaidigital.persistence.repository.WithdrawalRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class TimeDepositWithdrawalServiceImpl(
    private val timeDepositRepository: TimeDepositRepository,
    private val withdrawalRepository: WithdrawalRepository
) : TimeDepositWithdrawalService {

    private val logger = LoggerFactory.getLogger(TimeDepositWithdrawalServiceImpl::class.java)

    @Transactional
    override fun withdrawTimeDeposit(depositId: Int, amount: Double, withdrawalDate: LocalDate): BigDecimal {
        logger.info("Withdrawing amount: $amount from deposit ID: $depositId on date: $withdrawalDate")
        if (amount < 0) {
            throw IllegalArgumentException("Withdrawal amount must be positive")
        }

        val timeDepositEntity = timeDepositRepository.findById(depositId)
            .orElseThrow { TimeDepositNotFoundException("Time deposit with ID $depositId not found") }

        if (timeDepositEntity.balance < BigDecimal(amount)) {
            throw InsufficientBalanceException("Insufficient balance for withdrawal")
        }

        val updatedBalance = timeDepositEntity.balance - BigDecimal(amount)
        timeDepositEntity.balance = updatedBalance.setScale(2, RoundingMode.HALF_UP)

        val withdrawal = WithdrawalEntity(
            timeDeposit = timeDepositEntity,
            amount = BigDecimal(amount).setScale(2, RoundingMode.HALF_UP),
            date = withdrawalDate
        )

        withdrawalRepository.save(withdrawal)
        timeDepositRepository.save(timeDepositEntity)
        logger.info("Withdrawal successful, updated balance: $updatedBalance")
        return updatedBalance.setScale(2, RoundingMode.HALF_UP)
    }
}