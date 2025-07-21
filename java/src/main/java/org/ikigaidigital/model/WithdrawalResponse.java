package org.ikigaidigital.model;

import java.math.BigDecimal;

public record WithdrawalResponse(
    String id,
    BigDecimal amount,
    String date
) {}
