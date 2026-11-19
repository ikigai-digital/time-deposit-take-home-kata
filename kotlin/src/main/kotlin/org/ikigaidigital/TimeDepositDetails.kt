package org.ikigaidigital

import java.math.BigDecimal

data class TimeDepositDetails(
    val id: Int,
    val planType: String,
    val balance: BigDecimal,
    val days: Int,
    val withdrawals: List<Withdrawal>,
)
