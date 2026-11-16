package org.ikigaidigital.adapter.in.rest;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.adapter.in.rest.model.TimeDepositDto;
import org.ikigaidigital.adapter.in.rest.model.WithdrawalDto;
import org.ikigaidigital.application.TimeDepositWithWithdrawals;
import org.ikigaidigital.domain.Withdrawal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TimeDepositRestMapper {

    private static final int BALANCE_SCALE = 2;

    public TimeDepositDto toTimeDepositDto(TimeDepositWithWithdrawals timeDepositWithWithdrawals) {
        TimeDeposit timeDeposit = timeDepositWithWithdrawals.timeDeposit();
        return new TimeDepositDto()
                .id(timeDeposit.getId())
                .planType(timeDeposit.getPlanType())
                .balance(BigDecimal.valueOf(timeDeposit.getBalance()).setScale(BALANCE_SCALE, RoundingMode.HALF_UP))
                .days(timeDeposit.getDays())
                .withdrawals(timeDepositWithWithdrawals.withdrawals().stream()
                        .map(this::toWithdrawalDto)
                        .toList());
    }

    private WithdrawalDto toWithdrawalDto(Withdrawal withdrawal) {
        return new WithdrawalDto()
                .id(withdrawal.id())
                .amount(withdrawal.amount())
                .date(withdrawal.date());
    }
}
