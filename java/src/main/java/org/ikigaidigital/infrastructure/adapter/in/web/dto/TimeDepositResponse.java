package org.ikigaidigital.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record TimeDepositResponse(
    int id,
    String planType,
    BigDecimal balance,
    int days,
    List<WithdrawalResponse> withdrawals
) {
}
