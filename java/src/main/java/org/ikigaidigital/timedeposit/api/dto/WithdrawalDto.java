package org.ikigaidigital.timedeposit.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WithdrawalDto(int id, BigDecimal amount, LocalDate date) {

}
