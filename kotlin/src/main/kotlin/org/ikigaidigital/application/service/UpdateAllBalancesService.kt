package org.ikigaidigital.application.service

import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.application.port.inbound.UpdateAllBalancesUseCase
import org.ikigaidigital.application.port.outbound.InterestPlanRepository
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations
import java.time.Clock
import java.time.YearMonth

@Service
class UpdateAllBalancesService(
    private val deposits: TimeDepositRepository,
    private val plans: InterestPlanRepository,
    private val transaction: TransactionOperations,
    private val clock: Clock,
    @Value("\${time-deposits.balance-update.batch-size}") private val batchSize: Int,
) : UpdateAllBalancesUseCase {

    override fun updateAllBalances() {
        val calculator = TimeDepositCalculator(plans.findAll())
        val month = YearMonth.now(clock)
        do {
            val updatedCount = transaction.execute { updateNextBatch(calculator, month) }!!
        } while (updatedCount == batchSize)
    }

    private fun updateNextBatch(calculator: TimeDepositCalculator, month: YearMonth): Int {
        val batch = deposits.findBatchToUpdate(month, batchSize)
        batch.forEach { deposit -> deposits.save(deposit.withMonthlyInterest(calculator, month)) }
        return batch.size
    }
}
