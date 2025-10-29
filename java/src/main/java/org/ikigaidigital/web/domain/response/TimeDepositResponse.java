package org.ikigaidigital.web.domain.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
@Getter
@ToString
public final class TimeDepositResponse {
    private final int id;
    private final String planType;
    private final Double balance;
    private final int days;
    private final List<WithdrawalResponse> withdrawals;

    public TimeDepositResponse(int id, String planType, Double balance, int days, List<WithdrawalResponse> withdrawals) {
        this.id = id;
        this.planType = planType;
        this.balance = balance;
        this.days = days;
        this.withdrawals = withdrawals.stream()
                .map(withdrawal -> new WithdrawalResponse(withdrawal.getAmount(), withdrawal.getDate()))
                .collect(Collectors.toList());
    }
}
