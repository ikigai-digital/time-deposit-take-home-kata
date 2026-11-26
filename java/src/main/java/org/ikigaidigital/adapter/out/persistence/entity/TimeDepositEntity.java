package org.ikigaidigital.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private Integer id;

    @Column(name = "planType", nullable = false)
    private String planType;

    @Column(nullable = false)
    private Integer days;

    @Column(nullable = false)
    private BigDecimal balance;

    @OneToMany(mappedBy = "timeDeposit")
    private List<WithdrawalEntity> withdrawals = new ArrayList<>();

    protected TimeDepositEntity() {
    }

    public Integer getId() {
        return id;
    }

    public String getPlanType() {
        return planType;
    }

    public Integer getDays() {
        return days;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<WithdrawalEntity> getWithdrawals() {
        return withdrawals;
    }
}
