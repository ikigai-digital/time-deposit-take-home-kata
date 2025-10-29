package org.ikigaidigital.web.domain.response;

import java.time.OffsetDateTime;

public final class WithdrawalResponse {

    private final Double amount;
    private final OffsetDateTime date;

    public WithdrawalResponse(Double amount, OffsetDateTime date) {
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
