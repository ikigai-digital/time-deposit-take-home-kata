package org.ikigaidigital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "WITHDRAWALS")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WithdrawalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "AMOUNT")
    private Double amount;
    @Column(name = "DATE")
    private OffsetDateTime date;
    @ManyToOne
    @JoinColumn(name = "TIMEDEPOSITID")
    private TimeDepositEntity timeDeposit;
}
