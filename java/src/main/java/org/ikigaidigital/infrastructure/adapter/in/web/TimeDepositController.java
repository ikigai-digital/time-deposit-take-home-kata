package org.ikigaidigital.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/time-deposits")
public class TimeDepositController {

  public TimeDepositController() {
  }

  @GetMapping
  public String getAllTimeDeposits() {
    return "Time deposits retrieved (mock)";
  }

  @PostMapping("/update-balances")
  public String updateBalances() {
    return "Balances updated (mock)";
  }
}
