package org.ikigaidigital.adapter.outbound.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "withdrawals")
class WithdrawalEntity(
    @Id
    val id: Int,

    @Column(nullable = false)
    val timeDepositId: Int,

    @Column(nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false)
    val date: LocalDate,
)
