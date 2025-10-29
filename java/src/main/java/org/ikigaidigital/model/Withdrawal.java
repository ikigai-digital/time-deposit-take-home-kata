package org.ikigaidigital.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.time.OffsetDateTime;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class Withdrawal {

    private final Double amount;
    private final OffsetDateTime date;
}
