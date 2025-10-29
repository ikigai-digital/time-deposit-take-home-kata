package org.ikigaidigital.web.controller;

import org.ikigaidigital.mapper.TimeDepositResponseMapper;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.service.TimeDepositService;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TimeDepositController {

    private final TimeDepositService timeDepositService;
    private final TimeDepositResponseMapper timeDepositResponseMapper;

    public TimeDepositController(TimeDepositService timeDepositService, TimeDepositResponseMapper timeDepositResponseMapper) {
        this.timeDepositService = timeDepositService;
        this.timeDepositResponseMapper = timeDepositResponseMapper;
    }

    @GetMapping(value = "/deposits")
    public List<TimeDepositResponse> getTimeDeposits() {

        List<TimeDepositWithWithdrawals> timeDeposits
                = timeDepositService.getTimeDeposits();

        List<TimeDepositResponse> response = timeDeposits.stream()
                .map(timeDeposit ->
                        timeDepositResponseMapper.map(timeDeposit)).toList();

        return response;
    }
}
