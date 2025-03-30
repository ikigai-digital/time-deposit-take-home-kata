package org.ikigaidigital.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.exception.TimeDepositNotFoundException
import org.ikigaidigital.mapper.TimeDepositEntityMapper
import org.ikigaidigital.persistence.repository.TimeDepositRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimeDepositRetrievalServiceImpl(
    private val timeDepositRepository: TimeDepositRepository
) : TimeDepositRetrievalService {

    private val logger = LoggerFactory.getLogger(TimeDepositRetrievalServiceImpl::class.java)

    @Transactional(readOnly = true)
    override fun getTimeDeposit(depositId: Int): TimeDeposit {
        logger.info("Fetching time deposit with ID: $depositId")
        return timeDepositRepository.findById(depositId)
            .orElseThrow { TimeDepositNotFoundException("Time deposit with ID $depositId not found") }
            .let { TimeDepositEntityMapper.toDomain(it) }
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<TimeDeposit> {
        logger.info("Fetching all time deposits")
        return timeDepositRepository.findAll().map { TimeDepositEntityMapper.toDomain(it) }
    }
}