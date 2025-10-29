package org.ikigaidigital.model;

import java.time.OffsetDateTime;

public class Withdrawal {

    private final Double amount;
    private final OffsetDateTime date;

    public Withdrawal(Double amount, OffsetDateTime date) {
        this.amount = amount;
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public OffsetDateTime getDate() {
        return date;
    }
}
