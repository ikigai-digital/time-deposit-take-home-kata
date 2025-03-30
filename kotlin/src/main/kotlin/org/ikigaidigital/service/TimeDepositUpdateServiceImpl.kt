package org.ikigaidigital.service

import org.ikigaidigital.mapper.TimeDepositEntityMapper
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class TimeDepositUpdateServiceImpl(
    private val timeDepositRepository: TimeDepositRepository
) : TimeDepositUpdateService {

    private val logger = LoggerFactory.getLogger(TimeDepositUpdateServiceImpl::class.java)

    @Transactional
    override fun updateAllBalances() {
        logger.info("Updating all balances")
        val timeDeposits = timeDepositRepository.findAll()
        timeDeposits.forEach { entity ->
            val timeDeposit = TimeDepositEntityMapper.toDomain(entity)
            timeDeposit.updateBalance()
            entity.balance = BigDecimal(timeDeposit.balance).setScale(2, RoundingMode.HALF_UP)
        }
        timeDepositRepository.saveAll(timeDeposits)
        logger.info("All balances updated")
    }
}