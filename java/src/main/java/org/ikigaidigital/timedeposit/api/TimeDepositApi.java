package org.ikigaidigital.timedeposit.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ikigaidigital.timedeposit.api.dto.BalanceUpdateResponseDto;
import org.ikigaidigital.timedeposit.api.dto.PageDto;
import org.ikigaidigital.timedeposit.api.dto.TimeDepositDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Time deposits")
@RequestMapping(path = "/api/time-deposits")
public interface TimeDepositApi {

  @Operation(summary = "Start the monthly interest update of all time deposits")
  @ApiResponses({
      @ApiResponse(responseCode = "202", description = "Update started in the background",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = BalanceUpdateResponseDto.class),
              examples = @ExampleObject(name = "started", value = """
                  {"processId": 1, "status": "IN_PROGRESS", "message": "Balance update started"}"""))),
      @ApiResponse(responseCode = "409", description = "An update is already in progress; nothing new was started",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = BalanceUpdateResponseDto.class),
              examples = @ExampleObject(name = "alreadyInProgress", value = """
                  {"processId": null, "status": "IN_PROGRESS", "message": "Balance update is already in progress"}""")))
  })
  @PostMapping("/balance-updates")
  ResponseEntity<BalanceUpdateResponseDto> updateBalances();

  @Operation(summary = "List time deposits with their withdrawals")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "One page of time deposits"),
      @ApiResponse(responseCode = "400", description = "Invalid 'page' or 'size'", content = @Content)
  })
  @GetMapping
  ResponseEntity<PageDto<TimeDepositDto>> getTimeDeposits(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "100" ) int size);
}
