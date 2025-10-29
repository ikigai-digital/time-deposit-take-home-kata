package org.ikigaidigital.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "withdrawals")
public class WithdrawalEntity {
    @Id
    private int id;
    private int timeDepositId;
    private Double amount;
    private OffsetDateTime date;

    public WithdrawalEntity() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimeDepositId() {
        return timeDepositId;
    }

    public void setTimeDepositId(int timeDepositId) {
        this.timeDepositId = timeDepositId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }
}
