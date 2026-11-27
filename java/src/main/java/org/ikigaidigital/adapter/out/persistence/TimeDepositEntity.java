package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * JPA mapping of the {@code time_deposits} table. Kept apart from the domain {@code TimeDeposit},
 * which must stay free of persistence concerns (and cannot change).
 * Ids are assigned by the data, not generated: the README defines {@code id} as a plain integer key.
 */
@Entity
@Table(name = "time_deposits")
class TimeDepositEntity {

    @Id
    private Integer id;

    @Column(name = "plan_type", nullable = false)
    private String planType;

    @Column(nullable = false)
    private int days;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected TimeDepositEntity() {
        // for JPA
    }

    Integer getId() {
        return id;
    }

    String getPlanType() {
        return planType;
    }

    int getDays() {
        return days;
    }

    BigDecimal getBalance() {
        return balance;
    }

    void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
