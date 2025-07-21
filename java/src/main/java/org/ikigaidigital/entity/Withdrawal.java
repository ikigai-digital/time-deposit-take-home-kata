package org.ikigaidigital.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Withdrawal {

    @Id
    @Column(name="id")
    private Integer id;

    @Column(name="time_deposit_id", insertable=false, updatable=false)
    private Integer timeDepositId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_deposit_id", referencedColumnName = "ID", insertable = false, updatable = false)
    private TimeDeposit timeDeposit;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Withdrawal that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(timeDepositId, that.timeDepositId) && Objects.equals(timeDeposit, that.timeDeposit) && Objects.equals(amount, that.amount) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeDepositId, timeDeposit, amount, date);
    }
}
