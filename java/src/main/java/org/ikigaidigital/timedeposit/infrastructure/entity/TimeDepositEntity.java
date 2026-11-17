package org.ikigaidigital.timedeposit.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "time_deposits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeDepositEntity {

	@Id
	private Integer id;

	@Column(name = "plan_type", nullable = false)
	private String planType;

	@Column(nullable = false)
	private Integer days;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;
}
