package org.ikigaidigital.entity;

import jakarta.persistence.Entity;
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
@Table(name = "withdrawals")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WithdrawalEntity {
    @Id
    private Integer id;
    private Double amount;
    private OffsetDateTime date;
    @ManyToOne
    @JoinColumn(name = "timeDepositId")
    private TimeDepositEntity timeDeposit;
}
