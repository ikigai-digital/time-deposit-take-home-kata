package org.ikigaidigital.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.in.TimeDepositUseCase;
import org.ikigaidigital.domain.model.Withdrawal;
import org.ikigaidigital.infrastructure.adapter.in.web.dto.TimeDepositResponse;
import org.ikigaidigital.infrastructure.adapter.in.web.dto.WithdrawalResponse;
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
  public List<TimeDepositResponse> getAllTimeDeposits() {
    return timeDepositUseCase.getAllDeposits()
        .stream()
        .map(this::toTimeDepositResponse)
        .toList();
  }

  @PostMapping("/update-balances")
  public void updateBalances() {
    timeDepositUseCase.updateAllBalances();
  }

  private TimeDepositResponse toTimeDepositResponse(TimeDeposit deposit) {
    return new TimeDepositResponse(
        deposit.getId(),
        deposit.getPlanType(),
        BigDecimal.valueOf(deposit.getBalance()).setScale(2, RoundingMode.HALF_UP),
        deposit.getDays(),
        deposit.getWithdrawals().stream().map(this::toWithdrawalResponse).toList()
    );
  }

  private WithdrawalResponse toWithdrawalResponse(Withdrawal withdrawals) {
    return new WithdrawalResponse(
        withdrawals.id(),
        withdrawals.amount(),
        withdrawals.date());
  }
}
