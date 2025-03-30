package org.ikigaidigital.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.exception.TimeDepositNotFoundException
import org.ikigaidigital.mapper.TimeDepositEntityMapper
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.ikigaidigital.strategy.InterestStrategy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class TimeDepositCalculationServiceImpl(
    private val timeDepositRepository: TimeDepositRepository,
    private val interestStrategies: Map<String, InterestStrategy>
) : TimeDepositCalculationService {

    private val logger = LoggerFactory.getLogger(TimeDepositCalculationServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun calculateMaturityAmount(deposit: TimeDeposit): BigDecimal {
        logger.info("Calculating maturity amount for deposit: $deposit")
        val strategy = interestStrategies[deposit.planType]
            ?: throw IllegalArgumentException("Unknown plan type: ${deposit.planType}")
        val interest = strategy.calculateInterest(deposit)
        val maturityAmount = BigDecimal(interest).setScale(2, RoundingMode.HALF_UP)
        logger.info("Calculated maturity amount: $maturityAmount")
        return maturityAmount
    }

    @Transactional(readOnly = true)
    override fun calculateMaturityAmountById(depositId: Int): BigDecimal {
        val deposit = timeDepositRepository.findById(depositId)
            .orElseThrow { TimeDepositNotFoundException("Time deposit with ID $depositId not found") }
            .let { TimeDepositEntityMapper.toDomain(it) }
        return calculateMaturityAmount(deposit)
    }
}