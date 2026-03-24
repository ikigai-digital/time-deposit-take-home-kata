package org.ikigaidigital.infrastructure.adapter.in.web;

import java.util.List;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.in.TimeDepositUseCase;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/time-deposits")
public class TimeDepositController {
  private final TimeDepositUseCase timeDepositUseCase;

  public TimeDepositController(TimeDepositUseCase timeDepositUseCase) {
    this.timeDepositUseCase = timeDepositUseCase;
  }

  @GetMapping
  public List<TimeDeposit> getAllTimeDeposits() {
    return timeDepositUseCase.getAllDeposits();
  }

  @PostMapping("/update-balances")
  public void updateBalances() {
    timeDepositUseCase.updateAllBalances();
  }
}
