package org.ikigaidigital.adapter.outbound.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "timeDeposits")
class TimeDepositEntity(
    @Id
    val id: Int,

    @Column(nullable = false)
    val planType: String,

    @Column(nullable = false)
    val days: Int,

    @Column(nullable = false, precision = 19, scale = 2)
    var balance: BigDecimal,

    var lastInterestMonth: LocalDate?,
)
