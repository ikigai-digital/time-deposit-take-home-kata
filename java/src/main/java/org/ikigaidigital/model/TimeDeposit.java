package org.ikigaidigital.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder(builderClassName = "Builder")
public class TimeDeposit {

    private Integer id;
    private String planType;

    @Setter
    private Double balance;
    private Integer days;

    public TimeDeposit(Integer id, String planType, Double balance, Integer days) {
        this.id = id;
        this.planType = planType.toUpperCase();
        this.balance = balance;
        this.days = days;
    }

}
