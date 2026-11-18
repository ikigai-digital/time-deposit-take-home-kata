package org.ikigaidigital.timedeposit.api;

import org.ikigaidigital.timedeposit.api.dto.BalanceUpdateResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/api/time-deposits")
public interface TimeDepositApi {

  @PostMapping("/balance-updates")
  ResponseEntity<BalanceUpdateResponseDto> updateBalances();
}
