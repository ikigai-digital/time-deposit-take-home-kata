package org.ikigaidigital.timedeposit.infrastructure.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimeDepositWithdrawalRow(
    Integer depositId,
    String planType,
    BigDecimal balance,
    Integer days,
    Integer withdrawalId,
    BigDecimal amount,
    LocalDate date) {

  public boolean hasWithdrawal() {
    return withdrawalId != null;
  }
}
