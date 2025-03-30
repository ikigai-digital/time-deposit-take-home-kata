package org.ikigaidigital.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "time_deposits")
class TimeDepositEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "plan_type", nullable = false)
    val planType: String,

    @Column(nullable = false)
    val days: Int,

    @Column(nullable = false, precision = 19, scale = 4)
    var balance: BigDecimal
)