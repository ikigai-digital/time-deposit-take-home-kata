package org.ikigaidigital

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@SpringBootTest(properties = ["time-deposits.balance-update.batch-size=2"])
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class TimeDepositApiIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val jdbc: JdbcTemplate,
) {

    @MockBean
    private lateinit var clock: Clock

    @BeforeEach
    fun setUp() {
        setNow("2026-11-20T10:00:00Z")
        jdbc.execute("DELETE FROM \"interestPlans\"")
        jdbc.update(
            """
            INSERT INTO "interestPlans" ("planType", "annualRate", "interestStartsAfterDays", "interestEndsAfterDays") VALUES
              ('basic', 0.0100, 30, NULL),
              ('student', 0.0300, 30, 365),
              ('premium', 0.0500, 45, NULL)
            """
        )
        jdbc.execute("DELETE FROM \"withdrawals\"")
        jdbc.execute("DELETE FROM \"timeDeposits\"")
        jdbc.update(
            """
            INSERT INTO "timeDeposits" ("id", "planType", "days", "balance") VALUES
              (1, 'basic', 45, 1234567.00),
              (2, 'student', 366, 1000.00),
              (3, 'premium', 46, 1000.00),
              (4, 'basic', 30, 1000.00),
              (5, 'basic', 31, 90071992547409.93)
            """
        )
        jdbc.update(
            """
            INSERT INTO "withdrawals" ("id", "timeDepositId", "amount", "date") VALUES
              (1, 1, 20.00, '2026-02-01'),
              (2, 1, 100.50, '2026-01-15')
            """
        )
    }

    @Test
    fun `GET returns time deposits with their withdrawals ordered by date and exact balances`() {
        mockMvc.get("/time-deposits").andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "items":[
                        {"id":1,"planType":"basic","balance":1234567.00,"days":45,"withdrawals":[
                          {"id":2,"amount":100.50,"date":"2026-01-15"},
                          {"id":1,"amount":20.00,"date":"2026-02-01"}]},
                        {"id":2,"planType":"student","balance":1000.00,"days":366,"withdrawals":[]},
                        {"id":3,"planType":"premium","balance":1000.00,"days":46,"withdrawals":[]},
                        {"id":4,"planType":"basic","balance":1000.00,"days":30,"withdrawals":[]},
                        {"id":5,"planType":"basic","balance":90071992547409.93,"days":31,"withdrawals":[]}
                      ],
                      "page":0,"size":100,"totalElements":5,"totalPages":1
                    }
                    """,
                    strict = true,
                )
            }
        }
    }

    @Test
    fun `GET pages through deposits ordered by id with totals`() {
        assertThat(pageIds("page=0&size=2")).containsExactly(1, 2)
        assertThat(pageIds("page=1&size=2")).containsExactly(3, 4)
        assertThat(pageIds("page=2&size=2")).containsExactly(5)
        assertThat(pageIds("page=3&size=2")).isEmpty()

        mockMvc.get("/time-deposits?page=3&size=2").andExpect {
            content { json("""{"page":3,"size":2,"totalElements":5,"totalPages":3}""") }
        }
    }

    @Test
    fun `GET clamps out-of-range page parameters`() {
        mockMvc.get("/time-deposits?page=-1&size=5000").andExpect {
            content { json("""{"page":0,"size":1000,"totalElements":5,"totalPages":1}""") }
        }
    }

    @Test
    fun `POST balance-updates applies one month of interest in batches without losing precision`() {
        postBalanceUpdate()

        assertThat(storedBalances()).containsExactly(
            "1235595.81", "1000.00", "1004.17", "1000.00", "90147052541199.44",
        )
    }

    @Test
    fun `POST balance-updates is idempotent within a month`() {
        repeat(3) { postBalanceUpdate() }

        assertThat(storedBalances()[2]).isEqualTo("1004.17")
    }

    @Test
    fun `parallel balance updates apply interest only once`() {
        val executor = Executors.newFixedThreadPool(8)
        try {
            executor.invokeAll(List(8) { Callable { postBalanceUpdate() } }).forEach { it.get() }
        } finally {
            executor.shutdown()
        }

        assertThat(storedBalances()).containsExactly(
            "1235595.81", "1000.00", "1004.17", "1000.00", "90147052541199.44",
        )
    }

    @Test
    fun `next month's balance update adds another month of interest`() {
        postBalanceUpdate()
        setNow("2026-12-01T00:00:00Z")
        postBalanceUpdate()

        assertThat(storedBalances()[2]).isEqualTo("1008.35")
    }

    @Test
    fun `balance update uses the interest plans stored in the database`() {
        jdbc.update("UPDATE \"interestPlans\" SET \"annualRate\" = 0.1200 WHERE \"planType\" = 'basic'")

        postBalanceUpdate()

        assertThat(storedBalances()[0]).isEqualTo("1246912.67")
    }

    @Test
    fun `Swagger UI serves the static contract and no generated api-docs`() {
        mockMvc.get("/openapi.yaml").andExpect { status { isOk() } }
        mockMvc.get("/swagger-ui.html").andExpect { status { isOk() } }
        mockMvc.get("/webjars/swagger-ui/swagger-ui-bundle.js").andExpect { status { isOk() } }
        mockMvc.get("/v3/api-docs").andExpect { status { isNotFound() } }
    }

    private fun setNow(instant: String) {
        `when`(clock.instant()).thenReturn(Instant.parse(instant))
        `when`(clock.zone).thenReturn(ZoneOffset.UTC)
    }

    private fun postBalanceUpdate() {
        mockMvc.post("/time-deposits/balance-updates").andExpect { status { isNoContent() } }
    }

    private fun pageIds(query: String): List<Int> =
        JsonPath.read(mockMvc.get("/time-deposits?$query").andReturn().response.contentAsString, "$.items[*].id")

    private fun storedBalances(): List<String> =
        jdbc.queryForList("SELECT \"balance\" FROM \"timeDeposits\" ORDER BY \"id\"", BigDecimal::class.java)
            .map(BigDecimal::toPlainString)
}
