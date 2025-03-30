package org.ikigaidigital.mapper

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.persistence.entity.TimeDepositEntity
import java.math.BigDecimal

object TimeDepositEntityMapper {
    fun toEntity(timeDeposit: TimeDeposit): TimeDepositEntity {
        return TimeDepositEntity(
            id = timeDeposit.id,
            planType = timeDeposit.planType,
            days = timeDeposit.days,
            balance = BigDecimal(timeDeposit.balance)
        )
    }

    fun toDomain(timeDepositEntity: TimeDepositEntity): TimeDeposit {
        return TimeDeposit(
            id = timeDepositEntity.id,
            planType = timeDepositEntity.planType,
            balance = timeDepositEntity.balance.toDouble(),
            days = timeDepositEntity.days
        )
    }
}