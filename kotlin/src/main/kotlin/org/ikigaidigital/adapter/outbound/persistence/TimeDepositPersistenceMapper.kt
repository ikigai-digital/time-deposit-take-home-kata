package org.ikigaidigital.adapter.outbound.persistence

import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.TimeDepositRecord
import org.ikigaidigital.Withdrawal
import java.time.YearMonth

internal object TimeDepositPersistenceMapper {

    fun toRecord(entity: TimeDepositEntity): TimeDepositRecord =
        TimeDepositRecord(
            id = entity.id,
            planType = entity.planType,
            days = entity.days,
            balance = entity.balance,
            lastInterestMonth = entity.lastInterestMonth?.let(YearMonth::from),
        )

    fun toDetails(entity: TimeDepositEntity, withdrawals: List<WithdrawalEntity>): TimeDepositDetails =
        TimeDepositDetails(
            id = entity.id,
            planType = entity.planType,
            balance = entity.balance,
            days = entity.days,
            withdrawals = withdrawals.map { Withdrawal(it.id, it.amount, it.date) },
        )
}
