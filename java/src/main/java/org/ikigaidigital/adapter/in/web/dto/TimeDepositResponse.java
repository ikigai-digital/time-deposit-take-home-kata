package org.ikigaidigital.adapter.in.web.dto;

import java.util.List;

/**
 * JSON representation from the OpenAPI {@code TimeDeposit} schema.
 * Balance is a decimal string because the column is PostgreSQL NUMERIC.
 */
public record TimeDepositResponse(
        int id,
        String planType,
        String balance,
        int days,
        List<WithdrawalResponse> withdrawals
) {
}
