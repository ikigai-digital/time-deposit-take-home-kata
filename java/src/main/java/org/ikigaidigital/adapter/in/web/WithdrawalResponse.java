package org.ikigaidigital.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ikigaidigital.Withdrawal;

import java.math.BigDecimal;
import java.time.LocalDate;

/** {@code timeDepositId} is omitted: withdrawals are always nested in their time deposit. */
@Schema(name = "Withdrawal")
record WithdrawalResponse(
        @Schema(example = "1") int id,
        @Schema(example = "500.00") BigDecimal amount,
        @Schema(example = "2026-01-15") LocalDate date) {

    static WithdrawalResponse from(Withdrawal withdrawal) {
        return new WithdrawalResponse(withdrawal.id(), withdrawal.amount(), withdrawal.date());
    }
}
