package org.ikigaidigital.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name = "timeDeposits")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TimeDepositEntity {
    @Id
    private Integer id;
    private String planType;
    private Double balance;
    private int days;
    @OneToMany(mappedBy = "timeDeposit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WithdrawalEntity> withdrawals;

}
