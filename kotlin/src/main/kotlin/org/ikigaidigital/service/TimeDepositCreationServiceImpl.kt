package org.ikigaidigital.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.mapper.TimeDepositEntityMapper
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class TimeDepositCreationServiceImpl(
    private val timeDepositRepository: TimeDepositRepository
) : TimeDepositCreationService {

    private val logger = LoggerFactory.getLogger(TimeDepositCreationServiceImpl::class.java)

    @Transactional
    override fun createTimeDeposit(
        amount: Double,
        termInMonths: Int,
        planType: String,
        startDate: LocalDate
    ): TimeDeposit {
        logger.info("Creating time deposit with amount: $amount, term: $termInMonths months, planType: $planType, startDate: $startDate")
        val timeDeposit = TimeDeposit(
            id = 0,
            planType = planType,
            balance = BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).toDouble(),
            days = termInMonths * 30
        )
        val entity = TimeDepositEntityMapper.toEntity(timeDeposit)
        val savedEntity = timeDepositRepository.save(entity)
        logger.info("Time deposit created with ID: ${savedEntity.id}")
        return TimeDepositEntityMapper.toDomain(savedEntity)
    }
}