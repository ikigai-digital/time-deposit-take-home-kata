package org.ikigaidigital.controller;

import org.ikigaidigital.model.TimeDepositResponse;
import org.ikigaidigital.service.TimeDepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/time-deposit")
public class TimeDepositController {

    private final TimeDepositService timeDepositService;

    @Autowired
    public TimeDepositController(TimeDepositService timeDepositService) {
        this.timeDepositService = timeDepositService;
    }

    @GetMapping
    public List<TimeDepositResponse> getAllTimeDeposits() {
        return timeDepositService.getAllTimeDeposits();
    }

    @PutMapping("/balances")
    public void updateBalances() {
        timeDepositService.updateBalances();
    }
}

