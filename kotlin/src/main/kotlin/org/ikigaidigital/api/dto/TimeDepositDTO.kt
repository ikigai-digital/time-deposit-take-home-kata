package org.ikigaidigital.api.dto

import java.math.BigDecimal

data class TimeDepositDTO(
    val id: Int,
    val planType: String,
    val balance: BigDecimal,
    val days: Int
)