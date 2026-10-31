package org.ikigaidigital.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.ikigaidigital.application.TimeDepositService;
import org.ikigaidigital.application.TimeDepositView;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Exposes exactly two business operations; Swagger documentation routes are infrastructure. */
@RestController
@RequestMapping("/time-deposits")
public class TimeDepositController {
    private final TimeDepositService service;

    public TimeDepositController(TimeDepositService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve all time deposits",
            description = "Returns deposits with their withdrawal history, ordered by ID. Returns an empty array when no deposits exist.")
    @ApiResponse(responseCode = "200", description = "All time deposits and their withdrawals")
    public List<TimeDepositView> getAllDeposits() {
        // Return the service's shared view records; Spring writes them as JSON.
        return service.getAllDeposits();
    }

    @PostMapping("/update-balances")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update all time deposit balances",
            description = "Applies one interest calculation to current balances. Repeated calls can add interest again; days and withdrawal history remain unchanged. No request body is needed.")
    @ApiResponse(responseCode = "204", description = "Balances updated", content = @Content)
    public void updateAllBalances() {
        // All calculation, persistence, and transaction coordination belong to the service.
        service.updateAllBalances();
    }
}
