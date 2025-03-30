package org.ikigaidigital.mapper

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.api.dto.TimeDepositDTO
import java.math.BigDecimal

object TimeDepositDtoMapper {
    fun toDTO(timeDeposit: TimeDeposit): TimeDepositDTO {
        return TimeDepositDTO(
            id = timeDeposit.id,
            planType = timeDeposit.planType,
            balance = BigDecimal(timeDeposit.balance),
            days = timeDeposit.days
        )
    }

    fun fromDTO(timeDepositDTO: TimeDepositDTO): TimeDeposit {
        return TimeDeposit(
            id = timeDepositDTO.id,
            planType = timeDepositDTO.planType,
            balance = timeDepositDTO.balance.toDouble(),
            days = timeDepositDTO.days
        )
    }
}