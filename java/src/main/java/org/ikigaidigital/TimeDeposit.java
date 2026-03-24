package org.ikigaidigital;

import java.util.ArrayList;
import java.util.List;
import org.ikigaidigital.domain.model.Withdrawal;

public class TimeDeposit {
    private int id;
    private String planType;
    private Double balance;
    private int days;
    private List<Withdrawal> withdrawals = new ArrayList<>();

    public TimeDeposit(int id, String planType, Double balance, int days) {
        this.id = id;
        this.planType = planType;
        this.balance = balance;
        this.days = days;
    }

    public int getId() { return id; }

    public String getPlanType() {
        return planType;
    }

    public Double getBalance() {
        return balance;
    }

    public int getDays() {
        return days;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public List<Withdrawal> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(List<Withdrawal> withdrawals) {
        this.withdrawals = withdrawals;
    }

}
