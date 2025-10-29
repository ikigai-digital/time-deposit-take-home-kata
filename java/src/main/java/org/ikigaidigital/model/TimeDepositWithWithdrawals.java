package org.ikigaidigital.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
@ToString
@Getter
public class TimeDepositWithWithdrawals {
    private final int id;
    private final String planType;
    private final Double balance;
    private final int days;
    private final List<Withdrawal> withdrawals;

    public TimeDepositWithWithdrawals(int id, String planType, Double balance, int days, List<Withdrawal> withdrawals) {
        this.id = id;
        this.planType = planType;
        this.balance = balance;
        this.days = days;
        this.withdrawals = withdrawals.stream()
                .map(withdrawal -> new Withdrawal(withdrawal.getAmount(), withdrawal.getDate()))
                .collect(Collectors.toList());
    }
}
