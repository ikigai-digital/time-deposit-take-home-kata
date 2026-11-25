package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA mapping of the {@code withdrawals} table. The foreign key is mapped as a plain column rather than
 * a {@code @ManyToOne}: the domain {@code Withdrawal} only carries the id, and withdrawals are read
 * in bulk, so an association would only add lazy-loading pitfalls.
 */
@Entity
@Table(name = "withdrawals")
class WithdrawalEntity {

    @Id
    private Integer id;

    @Column(name = "time_deposit_id", nullable = false)
    private int timeDepositId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    protected WithdrawalEntity() {
        // for JPA
    }

    Integer getId() {
        return id;
    }

    int getTimeDepositId() {
        return timeDepositId;
    }

    BigDecimal getAmount() {
        return amount;
    }

    LocalDate getDate() {
        return date;
    }
}
