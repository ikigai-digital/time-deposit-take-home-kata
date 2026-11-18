package org.ikigaidigital.timedeposit.api;

import org.ikigaidigital.timedeposit.api.dto.BalanceUpdateResponseDto;
import org.ikigaidigital.timedeposit.api.dto.PageDto;
import org.ikigaidigital.timedeposit.api.dto.TimeDepositDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "/api/time-deposits")
public interface TimeDepositApi {

  @PostMapping("/balance-updates")
  ResponseEntity<BalanceUpdateResponseDto> updateBalances();

  @GetMapping
  ResponseEntity<PageDto<TimeDepositDto>> getTimeDeposits(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "100" ) int size);
}
