package org.ikigaidigital.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Inbound web adapter. Exactly the two endpoints the README asks for.
 */
@RestController
@RequestMapping("/time-deposits")
@Tag(name = "Time deposits")
class TimeDepositController {

    private final GetAllTimeDepositsUseCase getAllTimeDeposits;
    private final UpdateAllBalancesUseCase updateAllBalances;

    TimeDepositController(GetAllTimeDepositsUseCase getAllTimeDeposits, UpdateAllBalancesUseCase updateAllBalances) {
        this.getAllTimeDeposits = getAllTimeDeposits;
        this.updateAllBalances = updateAllBalances;
    }

    @GetMapping
    @Operation(summary = "List all time deposits", description = "Returns every time deposit, ordered by id, "
            + "with its withdrawals ordered by date.")
    @ApiResponse(responseCode = "200", description = "All time deposits")
    List<TimeDepositResponse> getAll() {
        return getAllTimeDeposits.getAllTimeDeposits().stream()
                .map(TimeDepositResponse::from)
                .toList();
    }

    /**
     * POST, not PUT: the operation is not idempotent; every call accrues another month of interest.
     * Modelled as creating a "balance update" (a command resource), since it acts on the whole collection.
     * No body is returned; clients read the new balances with GET (the README limits the API to two endpoints).
     */
    @PostMapping("/balance-updates")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update the balances of all time deposits",
            description = "Applies one month of interest to every time deposit, according to its plan. "
                    + "Not idempotent: each call accrues another month.")
    @ApiResponse(responseCode = "204", description = "All balances updated")
    void updateAllBalances() {
        updateAllBalances.updateAllBalances();
    }
}
