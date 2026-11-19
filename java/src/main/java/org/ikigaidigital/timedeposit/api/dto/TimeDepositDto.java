package org.ikigaidigital.timedeposit.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record TimeDepositDto(
    int id,
    String planType,
    BigDecimal balance,
    int days,
    List<WithdrawalDto> withdrawals) {

}
