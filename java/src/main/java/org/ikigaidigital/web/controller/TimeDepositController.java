package org.ikigaidigital.web.controller;

import com.github.fge.jsonpatch.JsonPatch;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ikigaidigital.exception.InternalServerErrorException;
import org.ikigaidigital.exception.TimeDepositNotFoundException;
import org.ikigaidigital.mapper.TimeDepositResponseMapper;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.service.TimeDepositService;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@RestController
@AllArgsConstructor
@Slf4j
public class TimeDepositController {

    private static final String ERROR_MSG_GET_DEPOSITS = "Error while calling: GET /deposits";
    private static final String ERROR_MSG_UPDATE_DEPOSITS = "Error while updating time deposit";

    private final TimeDepositService timeDepositService;
    private final TimeDepositResponseMapper timeDepositResponseMapper;

    @GetMapping(value = "/deposits")
    public List<TimeDepositResponse> getTimeDeposits() {

        log.info("Initiate GET /deposits");

        List<TimeDepositResponse> response = List.of();

        try {
            List<TimeDepositWithWithdrawals> timeDeposits
                    = timeDepositService.getTimeDeposits();

            if (isNotEmpty(timeDeposits)) {
                response = timeDeposits.stream()
                        .map(timeDeposit ->
                                timeDepositResponseMapper.map(timeDeposit)).toList();
            }
        } catch (Exception exception) {
            log.error(ERROR_MSG_GET_DEPOSITS);
            throw new InternalServerErrorException(ERROR_MSG_GET_DEPOSITS, exception);
        }

        log.info("GET /deposits response={}", response);

        return response;
    }

    @PatchMapping(path = "/deposits/{id}", consumes = "application/json-patch+json")
    public TimeDepositResponse updateTimeDeposit(@PathVariable int id,
                                                 @RequestBody JsonPatch updates) {

        try {
            TimeDepositWithWithdrawals updatedTimeDeposit = timeDepositService.update(id, updates);
            return timeDepositResponseMapper.map(updatedTimeDeposit);
        } catch (TimeDepositNotFoundException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InternalServerErrorException(ERROR_MSG_UPDATE_DEPOSITS, exception);
        } finally {
            log.error(ERROR_MSG_GET_DEPOSITS);
        }
    }
}
