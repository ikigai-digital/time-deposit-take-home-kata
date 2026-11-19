package org.ikigaidigital.adapter.outbound.persistence

import org.ikigaidigital.TimeDepositPage
import org.ikigaidigital.TimeDepositRecord
import org.ikigaidigital.application.port.outbound.TimeDepositPageRepository
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.YearMonth

@Component
class TimeDepositPersistenceAdapter(
    private val timeDeposits: TimeDepositJpaRepository,
    private val withdrawals: WithdrawalJpaRepository,
) : TimeDepositRepository, TimeDepositPageRepository {

    override fun findPage(page: Int, size: Int): TimeDepositPage {
        val deposits = timeDeposits.findAll(PageRequest.of(page, size, Sort.by("id")))
        val withdrawalsByDepositId =
            if (deposits.isEmpty) emptyMap()
            else withdrawals.findByTimeDepositIdInOrderByDateAscIdAsc(deposits.map { it.id }.toList())
                .groupBy { it.timeDepositId }
        return TimeDepositPage(
            items = deposits.map { TimeDepositPersistenceMapper.toDetails(it, withdrawalsByDepositId[it.id].orEmpty()) }.toList(),
            page = page,
            size = size,
            totalElements = deposits.totalElements,
        )
    }

    override fun findBatchToUpdate(month: YearMonth, batchSize: Int): List<TimeDepositRecord> =
        timeDeposits.lockNextBatchToUpdate(month.atDay(1), batchSize).map(TimeDepositPersistenceMapper::toRecord)

    override fun save(deposit: TimeDepositRecord) {
        val entity = timeDeposits.getReferenceById(deposit.id)
        entity.balance = deposit.balance
        entity.lastInterestMonth = deposit.lastInterestMonth?.atDay(1)
        timeDeposits.save(entity)
    }
}
