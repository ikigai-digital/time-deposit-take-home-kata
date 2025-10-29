package org.ikigaidigital.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.List;


@Entity
@Table(name = "timeDeposits")
public class TimeDepositEntity {
    @Id
    private int id;
    private String planType;
    private Double balance;
    private int days;
    private List<WithdrawalEntity> withdrawals;

    public TimeDepositEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public List<WithdrawalEntity> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(List<WithdrawalEntity> withdrawals) {
        this.withdrawals = withdrawals;
    }
}
