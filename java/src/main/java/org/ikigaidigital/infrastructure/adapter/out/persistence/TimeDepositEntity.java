package org.ikigaidigital.infrastructure.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timeDeposits")
public class TimeDepositEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String planType;

    @Column(nullable = false)
    private int days;

    @Column(nullable = false)
    private BigDecimal balance;

    @OneToMany(mappedBy = "timeDeposit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WithdrawalEntity> withdrawals = new ArrayList<>();

    public TimeDepositEntity() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<WithdrawalEntity> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(List<WithdrawalEntity> withdrawals) {
        this.withdrawals = withdrawals;
    }
}
