package org.ikigaidigital.timedeposit.api;

import lombok.RequiredArgsConstructor;
import org.ikigaidigital.timedeposit.api.dto.BalanceUpdateResponseDto;
import org.ikigaidigital.timedeposit.domain.BalanceUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimeDepositController implements TimeDepositApi {

  private final BalanceUpdateService balanceUpdateService;

  @Override
  public ResponseEntity<BalanceUpdateResponseDto> updateBalances() {
    return balanceUpdateService.startUpdate()
        .map(processId -> ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(new BalanceUpdateResponseDto(processId, "IN_PROGRESS", "Balance update started")))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new BalanceUpdateResponseDto(null, "IN_PROGRESS", "Balance update is already in progress")));
  }
}
