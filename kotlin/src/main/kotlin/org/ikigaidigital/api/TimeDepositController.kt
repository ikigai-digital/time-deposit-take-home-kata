package org.ikigaidigital.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.ikigaidigital.api.dto.TimeDepositDTO
import org.ikigaidigital.mapper.TimeDepositDtoMapper
import org.ikigaidigital.service.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate

@RestController
@RequestMapping("/api/time-deposits")
@Tag(name = "Time Deposit API", description = "API for managing time deposits")
class TimeDepositController(
    private val timeDepositCreationService: TimeDepositCreationService,
    private val timeDepositCalculationService: TimeDepositCalculationService,
    private val timeDepositWithdrawalService: TimeDepositWithdrawalService,
    private val timeDepositRetrievalService: TimeDepositRetrievalService,
    private val timeDepositUpdateService: TimeDepositUpdateService
) {

    private val logger: Logger = LoggerFactory.getLogger(TimeDepositController::class.java)

    @PostMapping
    @Operation(summary = "Create a new time deposit")
    fun createTimeDeposit(
        @RequestParam amount: BigDecimal,
        @RequestParam termInMonths: Int,
        @RequestParam planType: String,
        @RequestParam startDate: LocalDate
    ): ResponseEntity<TimeDepositDTO> {
        logger.info("Creating time deposit with amount: $amount, term: $termInMonths months, plan: $planType, start date: $startDate")
        val timeDeposit = timeDepositCreationService.createTimeDeposit(amount.toDouble(), termInMonths, planType, startDate)
        return ResponseEntity.status(HttpStatus.CREATED).body(TimeDepositDtoMapper.toDTO(timeDeposit))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a time deposit by ID")
    fun getTimeDeposit(@PathVariable id: Int): ResponseEntity<TimeDepositDTO> {
        logger.info("Fetching time deposit with ID: $id")
        val timeDeposit = timeDepositRetrievalService.getTimeDeposit(id)
        return ResponseEntity.ok(TimeDepositDtoMapper.toDTO(timeDeposit))
    }

    @GetMapping("/{id}/maturity-amount")
    @Operation(summary = "Calculate the maturity amount for a time deposit")
    fun calculateMaturityAmount(@PathVariable id: Int): ResponseEntity<BigDecimal> {
        logger.info("Calculating maturity amount for time deposit with ID: $id")
        val maturityAmount = timeDepositCalculationService.calculateMaturityAmountById(id)
        return ResponseEntity.ok(maturityAmount)
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw an amount from a time deposit")
    fun withdrawTimeDeposit(
        @PathVariable id: Int,
        @RequestParam amount: BigDecimal,
        @RequestParam withdrawalDate: LocalDate
    ): ResponseEntity<BigDecimal> {
        logger.info("Withdrawing amount: $amount from time deposit with ID: $id on date: $withdrawalDate")
        val remainingBalance = timeDepositWithdrawalService.withdrawTimeDeposit(id, amount.toDouble(), withdrawalDate)
        return ResponseEntity.ok(remainingBalance)
    }

    @PutMapping("/update-balances")
    @Operation(summary = "Update the balance of all time deposits")
    fun updateAllBalances(): ResponseEntity<Map<String, Any>> {
        logger.info("Updating balances for all time deposits")
        timeDepositUpdateService.updateAllBalances()
        return ResponseEntity.ok(mapOf("success" to true, "message" to "All balances updated successfully"))
    }

    @GetMapping
    @Operation(summary = "Get all time deposits")
    fun getAllTimeDeposits(): ResponseEntity<List<TimeDepositDTO>> {
        logger.info("Fetching all time deposits")
        val timeDeposits = timeDepositRetrievalService.findAll()
        return ResponseEntity.ok(timeDeposits.map { TimeDepositDtoMapper.toDTO(it) })
    }
}