package org.ikigaidigital.model;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<Withdrawal> getWithdrawals() {
        return withdrawals;
    }
}
