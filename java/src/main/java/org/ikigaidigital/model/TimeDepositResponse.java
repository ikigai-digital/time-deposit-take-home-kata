package org.ikigaidigital.model;

import java.math.BigDecimal;
import java.util.List;

public record TimeDepositResponse(
    String id,
    String planType,
    BigDecimal balance,
    int days,
    List<WithdrawalResponse> withdrawals
) {}
