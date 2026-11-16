package org.ikigaidigital.adapter.in.rest;

import org.ikigaidigital.adapter.in.rest.api.TimeDepositsApi;
import org.ikigaidigital.adapter.in.rest.model.TimeDepositDto;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TimeDepositController implements TimeDepositsApi {

    private final GetAllTimeDepositsUseCase getAllTimeDepositsUseCase;
    private final UpdateAllBalancesUseCase updateAllBalancesUseCase;
    private final TimeDepositRestMapper timeDepositRestMapper;

    public TimeDepositController(GetAllTimeDepositsUseCase getAllTimeDepositsUseCase,
                                 UpdateAllBalancesUseCase updateAllBalancesUseCase,
                                 TimeDepositRestMapper timeDepositRestMapper) {
        this.getAllTimeDepositsUseCase = getAllTimeDepositsUseCase;
        this.updateAllBalancesUseCase = updateAllBalancesUseCase;
        this.timeDepositRestMapper = timeDepositRestMapper;
    }

    @Override
    public ResponseEntity<List<TimeDepositDto>> getAllTimeDeposits() {
        List<TimeDepositDto> timeDeposits = getAllTimeDepositsUseCase.getAllTimeDeposits().stream()
                .map(timeDepositRestMapper::toTimeDepositDto)
                .toList();
        return ResponseEntity.ok(timeDeposits);
    }

    @Override
    public ResponseEntity<Void> updateAllBalances() {
        updateAllBalancesUseCase.updateAllBalances();
        return ResponseEntity.noContent().build();
    }
}
