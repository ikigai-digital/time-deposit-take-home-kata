import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.api.TimeDepositController
import org.ikigaidigital.api.dto.TimeDepositDTO
import org.ikigaidigital.mapper.TimeDepositDtoMapper
import org.ikigaidigital.service.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.math.BigDecimal
import java.time.LocalDate

class TimeDepositControllerTest {

    private lateinit var timeDepositCreationService: TimeDepositCreationService
    private lateinit var timeDepositCalculationService: TimeDepositCalculationService
    private lateinit var timeDepositWithdrawalService: TimeDepositWithdrawalService
    private lateinit var timeDepositRetrievalService: TimeDepositRetrievalService
    private lateinit var timeDepositUpdateService: TimeDepositUpdateService
    private lateinit var timeDepositController: TimeDepositController

    @BeforeEach
    fun setUp() {
        timeDepositCreationService = mock(TimeDepositCreationService::class.java)
        timeDepositCalculationService = mock(TimeDepositCalculationService::class.java)
        timeDepositWithdrawalService = mock(TimeDepositWithdrawalService::class.java)
        timeDepositRetrievalService = mock(TimeDepositRetrievalService::class.java)
        timeDepositUpdateService = mock(TimeDepositUpdateService::class.java)
        timeDepositController = TimeDepositController(
            timeDepositCreationService,
            timeDepositCalculationService,
            timeDepositWithdrawalService,
            timeDepositRetrievalService,
            timeDepositUpdateService
        )
    }

    @Test
    fun `createTimeDeposit should return created TimeDepositDTO`() {
        val amount = BigDecimal(1000)
        val termInMonths = 12
        val planType = "basic"
        val startDate = LocalDate.now()
        val timeDeposit = TimeDeposit(1, planType, amount.toDouble(), termInMonths * 30)
        `when`(timeDepositCreationService.createTimeDeposit(amount.toDouble(), termInMonths, planType, startDate)).thenReturn(timeDeposit)

        val response: ResponseEntity<TimeDepositDTO> = timeDepositController.createTimeDeposit(amount, termInMonths, planType, startDate)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(TimeDepositDtoMapper.toDTO(timeDeposit), response.body)
    }

    @Test
    fun `getTimeDeposit should return TimeDepositDTO`() {
        val timeDeposit = TimeDeposit(1, "basic", 1000.0, 360)
        `when`(timeDepositRetrievalService.getTimeDeposit(1)).thenReturn(timeDeposit)

        val response: ResponseEntity<TimeDepositDTO> = timeDepositController.getTimeDeposit(1)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(TimeDepositDtoMapper.toDTO(timeDeposit), response.body)
    }

    @Test
    fun `calculateMaturityAmount should return maturity amount`() {
        val maturityAmount = BigDecimal(1050.0)
        `when`(timeDepositCalculationService.calculateMaturityAmountById(1)).thenReturn(maturityAmount)

        val response: ResponseEntity<BigDecimal> = timeDepositController.calculateMaturityAmount(1)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(maturityAmount, response.body)
    }

    @Test
    fun `withdrawTimeDeposit should return remaining balance`() {
        val withdrawalAmount = BigDecimal(200.0)
        val remainingBalance = BigDecimal(800.0)
        `when`(timeDepositWithdrawalService.withdrawTimeDeposit(1, withdrawalAmount.toDouble(), LocalDate.now())).thenReturn(remainingBalance)

        val response: ResponseEntity<BigDecimal> = timeDepositController.withdrawTimeDeposit(1, withdrawalAmount, LocalDate.now())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(remainingBalance, response.body)
    }

    @Test
    fun `updateAllBalances should return success message`() {
        doNothing().`when`(timeDepositUpdateService).updateAllBalances()

        val response: ResponseEntity<Map<String, Any>> = timeDepositController.updateAllBalances()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(true, response.body?.get("success"))
        assertEquals("All balances updated successfully", response.body?.get("message"))
    }

    @Test
    fun `getAllTimeDeposits should return list of TimeDepositDTOs`() {
        val timeDeposits = listOf(TimeDeposit(1, "basic", 1000.0, 360))
        `when`(timeDepositRetrievalService.findAll()).thenReturn(timeDeposits)

        val response: ResponseEntity<List<TimeDepositDTO>> = timeDepositController.getAllTimeDeposits()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(timeDeposits.map { TimeDepositDtoMapper.toDTO(it) }, response.body)
    }
}