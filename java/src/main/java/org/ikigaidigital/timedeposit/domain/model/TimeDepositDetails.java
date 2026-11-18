package org.ikigaidigital.timedeposit.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record TimeDepositDetails(
    int id,
    String planType,
    BigDecimal balance, int days,
    List<Withdrawal> withdrawals) {

  public TimeDepositDetails {
    withdrawals = List.copyOf(withdrawals);
  }

}
