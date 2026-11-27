package org.ikigaidigital.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.in.TimeDepositDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Response schema required by the README: id, planType, balance, days, withdrawals. */
@Schema(name = "TimeDeposit", description = "A time deposit with its withdrawals")
record TimeDepositResponse(
        @Schema(example = "1") int id,
        @Schema(description = "Plan code; known plans: basic, student, premium", example = "basic") String planType,
        @Schema(description = "Current balance, in currency units with 2 decimal places", example = "10008.33")
        BigDecimal balance,
        @Schema(description = "Age of the deposit in days", example = "45") int days,
        List<WithdrawalResponse> withdrawals) {

    private static final int MONEY_SCALE = 2;

    static TimeDepositResponse from(TimeDepositDetails details) {
        TimeDeposit deposit = details.timeDeposit();
        return new TimeDepositResponse(
                deposit.getId(),
                deposit.getPlanType(),
                toMoney(deposit.getBalance()),
                deposit.getDays(),
                details.withdrawals().stream().map(WithdrawalResponse::from).toList());
    }

    /**
     * The domain balance is a {@code Double}; it is exposed as a decimal so clients get exact cents
     * (10008.33, never 10008.329999999998), consistent with the NUMERIC(19, 2) column.
     */
    private static BigDecimal toMoney(double amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
