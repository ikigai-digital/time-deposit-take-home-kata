package org.ikigaidigital.adapter.in.web;

import org.ikigaidigital.adapter.in.web.dto.TimeDepositResponse;
import org.ikigaidigital.application.port.in.ListTimeDepositsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TimeDepositController {

    private final ListTimeDepositsUseCase listTimeDeposits;

    public TimeDepositController(ListTimeDepositsUseCase listTimeDeposits) {
        this.listTimeDeposits = listTimeDeposits;
    }

    @GetMapping("/time-deposits")
    public List<TimeDepositResponse> listTimeDeposits() {
        return listTimeDeposits.listAll().stream()
                .map(TimeDepositResponseMapper::toResponse)
                .toList();
    }
}
