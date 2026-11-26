package org.ikigaidigital.adapter.in.web;

import org.ikigaidigital.adapter.in.web.dto.TimeDepositResponse;
import org.ikigaidigital.adapter.in.web.dto.WithdrawalResponse;
import org.ikigaidigital.domain.model.TimeDeposit;
import org.ikigaidigital.domain.model.Withdrawal;

final class TimeDepositResponseMapper {

    private TimeDepositResponseMapper() {
    }

    static TimeDepositResponse toResponse(TimeDeposit deposit) {
        return new TimeDepositResponse(
                deposit.id(),
                deposit.planType(),
                deposit.balance().toPlainString(),
                deposit.days(),
                deposit.withdrawals().stream().map(TimeDepositResponseMapper::toResponse).toList()
        );
    }

    private static WithdrawalResponse toResponse(Withdrawal withdrawal) {
        return new WithdrawalResponse(
                withdrawal.id(),
                withdrawal.timeDepositId(),
                withdrawal.amount().toPlainString(),
                withdrawal.date()
        );
    }
}
