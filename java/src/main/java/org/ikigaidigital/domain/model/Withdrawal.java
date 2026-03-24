package org.ikigaidigital.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Withdrawal(
    Integer id,
    BigDecimal amount,
    LocalDate date
) {}
