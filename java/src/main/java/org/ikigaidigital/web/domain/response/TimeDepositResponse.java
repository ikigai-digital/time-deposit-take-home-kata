package org.ikigaidigital.web.domain.response;

import java.util.List;
import java.util.stream.Collectors;

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

    public int getId() {
        return id;
    }

    public String getPlanType() {
        return planType;
    }

    public Double getBalance() {
        return balance;
    }

    public int getDays() {
        return days;
    }

    public List<WithdrawalResponse> getWithdrawals() {
        return withdrawals;
    }
}
