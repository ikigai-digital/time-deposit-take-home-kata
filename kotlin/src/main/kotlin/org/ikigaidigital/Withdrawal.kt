package org.ikigaidigital

import java.math.BigDecimal
import java.time.LocalDate

data class Withdrawal(
    val id: Int,
    val amount: BigDecimal,
    val date: LocalDate,
)
