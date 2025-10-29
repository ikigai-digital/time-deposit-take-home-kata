package org.ikigaidigital.web.domain.response;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.OffsetDateTime;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public final class WithdrawalResponse {

    private final Double amount;
    private final OffsetDateTime date;
}
