package org.ikigaidigital.model;

import lombok.*;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Getter
@Setter
public class TimeDeposit {
    private int id;
    private String planType;
    private Double balance;
    private int days;
}
