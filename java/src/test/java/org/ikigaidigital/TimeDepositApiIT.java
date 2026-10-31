package org.ikigaidigital;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ikigaidigital.adapter.persistence.JdbcTimeDepositStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

/** Exercises the real HTTP, service, migration, and PostgreSQL flow; Docker is required. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class TimeDepositApiIT {
    // One temporary database per test class, started and cleaned up by Testcontainers.
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        // Override the normal connection settings with this container's random port and credentials.
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper objectMapper;

    // Calls the real adapter normally; only the rollback test adds a failure after real SQL writes.
    @MockitoSpyBean
    private JdbcTimeDepositStore store;

    @BeforeEach
    void clearData() {
        // HTTP requests use their own transactions, so reset committed fixtures explicitly.
        // Leave Flyway's history table intact to verify the actual migration.
        jdbc.execute("TRUNCATE TABLE withdrawals, \"timeDeposits\"");
    }

    @Test
    void migrationCreatesTheRequiredKeysAndNonNullFields() {
        assertThat(jdbc.queryForObject(
                "SELECT success FROM flyway_schema_history WHERE version = '1'", Boolean.class)).isTrue();
        insertDeposit(1, "basic", "1200.001", 31);
        insertWithdrawal(1, 1, "25.123456789", LocalDate.of(2026, 10, 30));

        // Test the schema's required constraints, without adding application validation.
        assertThatThrownBy(() -> insertDeposit(1, "student", "1200", 31))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO "timeDeposits" (id, "planType", days, balance)
                VALUES (2, 'basic', 31, NULL)
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertWithdrawal(2, 999, "10", LocalDate.of(2026, 10, 30)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO withdrawals (id, "timeDepositId", amount, date)
                VALUES (2, 1, 10, NULL)
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void emptyDatabaseReturnsAnEmptyArrayAndCanBeUpdated() throws JsonProcessingException {
        JsonNode before = getJson("/time-deposits");
        assertThat(before.isArray()).isTrue();
        assertThat(before).isEmpty();

        updateBalances();

        assertThat(getJson("/time-deposits")).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM \"timeDeposits\"", Integer.class)).isZero();
    }

    @Test
    void getReturnsExactFieldsDecimalsDatesAndOrderedWithdrawalLists() throws JsonProcessingException {
        // Insert out of order; the response must order deposits and each withdrawal list by ID.
        insertDeposit(20, "student", "1200.001", 31);
        insertDeposit(10, "basic", "12345678901234567890.123456789", 31);
        insertWithdrawal(40, 20, "10.50", LocalDate.of(2026, 10, 30));
        insertWithdrawal(8, 20, "25.1234567890123456789", LocalDate.of(2026, 10, 29));

        JsonNode deposits = getJson("/time-deposits");
        assertThat(deposits.isArray()).isTrue();
        assertThat(deposits).hasSize(2);
        JsonNode basic = deposits.get(0);
        assertThat(basic.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("id", "planType", "balance", "days", "withdrawals");
        assertThat(basic.get("id").asInt()).isEqualTo(10);
        assertThat(basic.get("planType").asText()).isEqualTo("basic");
        assertThat(basic.get("days").asInt()).isEqualTo(31);
        assertThat(basic.get("balance").decimalValue())
                .isEqualByComparingTo("12345678901234567890.123456789");
        assertThat(basic.get("withdrawals").isArray()).isTrue();
        assertThat(basic.get("withdrawals")).isEmpty();

        JsonNode student = deposits.get(1);
        assertThat(student.get("id").asInt()).isEqualTo(20);
        assertThat(student.get("balance").decimalValue()).isEqualByComparingTo("1200.001");
        JsonNode withdrawals = student.get("withdrawals");
        assertThat(withdrawals).hasSize(2);
        assertThat(withdrawals.get(0).fieldNames()).toIterable()
                .containsExactlyInAnyOrder("id", "amount", "date");
        assertThat(withdrawals.get(0).get("id").asInt()).isEqualTo(8);
        assertThat(withdrawals.get(0).get("amount").decimalValue())
                .isEqualByComparingTo("25.1234567890123456789");
        assertThat(withdrawals.get(0).get("date").asText()).isEqualTo("2026-10-29");
        assertThat(withdrawals.get(1).get("id").asInt()).isEqualTo(40);
        assertThat(withdrawals.get(1).get("amount").decimalValue()).isEqualByComparingTo("10.50");
        assertThat(withdrawals.get(1).get("date").asText()).isEqualTo("2026-10-30");
    }

    @Test
    void updatePersistsAllPlanResultsWithoutChangingMetadataOrWithdrawals() throws JsonProcessingException {
        insertDeposit(1, "basic", "1200", 31);
        insertDeposit(2, "student", "1200", 31);
        insertDeposit(3, "premium", "1200", 46);
        insertDeposit(4, "basic", "1200", 30);
        insertDeposit(5, "student", "1200", 366);
        insertDeposit(6, "premium", "1200", 45);
        insertDeposit(7, "future", "1200", 46);
        insertWithdrawal(1, 2, "200.50", LocalDate.of(2026, 10, 30));
        JsonNode before = getJson("/time-deposits");
        List<Map<String, Object>> historyBefore = jdbc.queryForList("SELECT * FROM withdrawals ORDER BY id");

        updateBalances();

        JsonNode after = getJson("/time-deposits");
        String[] expectedBalances = {"1201", "1203", "1205", "1200", "1200", "1200", "1200"};
        assertThat(after).hasSize(expectedBalances.length);
        for (int index = 0; index < expectedBalances.length; index++) {
            assertThat(after.get(index).get("balance").decimalValue())
                    .isEqualByComparingTo(expectedBalances[index]);
            for (String field : List.of("id", "planType", "days", "withdrawals")) {
                assertThat(after.get(index).get(field)).isEqualTo(before.get(index).get(field));
            }
        }
        // A separate SQL read checks persisted values, not merely the HTTP response.
        assertThat(jdbc.queryForObject("SELECT balance FROM \"timeDeposits\" WHERE id = 3", BigDecimal.class))
                .isEqualByComparingTo("1205");
        assertThat(jdbc.queryForList("SELECT * FROM withdrawals ORDER BY id")).isEqualTo(historyBefore);
    }

    @Test
    void repeatedUpdateRequestsCompoundUsingPersistedBalances() throws JsonProcessingException {
        insertDeposit(1, "basic", "1000000.00", 31);

        updateBalances();
        assertThat(getJson("/time-deposits").get(0).get("balance").decimalValue())
                .isEqualByComparingTo("1000833.33");
        updateBalances();

        assertThat(getJson("/time-deposits").get(0).get("balance").decimalValue())
                .isEqualByComparingTo("1001667.36");
        assertThat(jdbc.queryForObject("SELECT balance FROM \"timeDeposits\" WHERE id = 1", BigDecimal.class))
                .isEqualByComparingTo("1001667.36");
    }

    @Test
    void concurrentUpdateRequestsDoNotLoseAnInterestCredit() throws Exception {
        insertDeposit(1, "basic", "1200", 31);
        ExecutorService requests = Executors.newFixedThreadPool(2);

        // Hold a real row lock on an independent connection while both HTTP requests start.
        try (Connection blocker = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            blocker.setAutoCommit(false);
            try (PreparedStatement lock = blocker.prepareStatement(
                    "SELECT id FROM \"timeDeposits\" WHERE id = 1 FOR UPDATE")) {
                lock.execute();
            }
            // Closing the statement keeps the row locked until this transaction ends.
            Future<?> first = requests.submit(this::updateBalances);
            Future<?> second = requests.submit(this::updateBalances);
            try {
                // Wait for observed database activity, not a sleep that guesses request timing.
                // Accept locks at either SELECT or UPDATE so the same test works after the fix.
                await().atMost(10, SECONDS).untilAsserted(() -> assertThat(jdbc.queryForObject("""
                        SELECT count(*) FROM pg_stat_activity
                        WHERE datname = current_database() AND wait_event_type = 'Lock'
                        """, Integer.class)).isEqualTo(2));
            } finally {
                // Release the lock even when synchronization fails, so requests can finish.
                blocker.rollback();
            }
            // The helper checks that each real request returns 204 with an empty body.
            first.get(15, SECONDS);
            second.get(15, SECONDS);
        } finally {
            requests.shutdownNow();
            assertThat(requests.awaitTermination(15, SECONDS)).isTrue();
        }

        // Two successful calls must retain both credits: 1200 -> 1201 -> 1202.
        assertThat(jdbc.queryForObject("SELECT balance FROM \"timeDeposits\" WHERE id = 1", BigDecimal.class))
                .isEqualByComparingTo("1202");
    }

    @Test
    void updatePreservesFractionalBalancesAndExistingBinaryRounding() throws JsonProcessingException {
        insertDeposit(1, "basic", "1200.001", 31);
        insertDeposit(2, "student", "6.00", 31);

        updateBalances();

        JsonNode deposits = getJson("/time-deposits");
        assertThat(deposits.get(0).get("balance").decimalValue()).isEqualByComparingTo("1201.001");
        // The shared calculator's existing double/BigDecimal conversion adds 0.01 here, not 0.02.
        assertThat(deposits.get(1).get("balance").decimalValue()).isEqualByComparingTo("6.01");
    }

    @Test
    void databaseFailureAfterTheBalanceBatchRollsBackEarlierWrites() throws JsonProcessingException {
        insertDeposit(1, "basic", "1200", 31);
        insertDeposit(2, "premium", "1200", 46);
        insertWithdrawal(1, 1, "25.50", LocalDate.of(2026, 10, 30));
        JsonNode before = getJson("/time-deposits");
        List<Map<String, Object>> historyBefore = jdbc.queryForList("SELECT * FROM withdrawals ORDER BY id");
        AtomicBoolean batchCompleted = new AtomicBoolean();
        JdbcTimeDepositStore storeSpy = AopTestUtils.getUltimateTargetObject(store);

        // Keep real SQL writes, then cause a real FK failure on the same server thread/transaction.
        // Failing after the batch also catches a missing service transaction, not just batch atomicity.
        doAnswer(invocation -> {
            invocation.callRealMethod();
            batchCompleted.set(true);
            jdbc.update("""
                    INSERT INTO withdrawals (id, "timeDepositId", amount, date)
                    VALUES (999, 999999, 1, DATE '2026-10-30')
                    """);
            return null;
        }).when(storeSpy).updateBalances(anyList());

        ResponseEntity<String> response = http.postForEntity("/time-deposits/update-balances", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(batchCompleted).isTrue();
        assertThat(getJson("/time-deposits")).isEqualTo(before);
        assertThat(jdbc.queryForList("SELECT * FROM withdrawals ORDER BY id")).isEqualTo(historyBefore);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM withdrawals WHERE id = 999", Integer.class)).isZero();
    }

    @Test
    void swaggerDocumentsExactlyTheTwoBusinessOperationsAndResponseShape() throws JsonProcessingException {
        JsonNode contract = getJson("/v3/api-docs");
        JsonNode paths = contract.get("paths");
        assertThat(paths.fieldNames()).toIterable()
                .containsExactlyInAnyOrder("/time-deposits", "/time-deposits/update-balances");
        assertThat(paths.get("/time-deposits").fieldNames()).toIterable().containsExactly("get");
        assertThat(paths.get("/time-deposits/update-balances").fieldNames()).toIterable().containsExactly("post");

        JsonNode responseSchema = paths.get("/time-deposits")
                .at("/get/responses/200/content/application~1json/schema");
        assertThat(responseSchema.get("type").asText()).isEqualTo("array");
        JsonNode depositSchema = contract.at(responseSchema.at("/items/$ref").asText().substring(1));
        assertThat(depositSchema.get("properties").fieldNames()).toIterable()
                .containsExactlyInAnyOrder("id", "planType", "balance", "days", "withdrawals");
        JsonNode withdrawalSchema = contract.at(depositSchema.at("/properties/withdrawals/items/$ref")
                .asText().substring(1));
        assertThat(withdrawalSchema.get("properties").fieldNames()).toIterable()
                .containsExactlyInAnyOrder("id", "amount", "date");

        JsonNode post = paths.get("/time-deposits/update-balances").get("post");
        assertThat(post.has("requestBody")).isFalse();
        assertThat(post.at("/responses/204").isMissingNode()).isFalse();
        assertThat(post.at("/responses/204").has("content")).isFalse();
        ResponseEntity<String> ui = http.getForEntity("/swagger-ui/index.html", String.class);
        assertThat(ui.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ui.getHeaders().getContentType()).isNotNull();
        assertThat(ui.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML)).isTrue();
    }

    private JsonNode getJson(String path) throws JsonProcessingException {
        ResponseEntity<String> response = http.getForEntity(path, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(response.getBody()).isNotBlank();
        // Parse response decimals exactly; a Double-based test reader could hide a precision loss.
        return objectMapper.reader().with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .readTree(response.getBody());
    }

    private void updateBalances() {
        ResponseEntity<String> response = http.postForEntity("/time-deposits/update-balances", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNullOrEmpty();
    }

    private void insertDeposit(int id, String planType, String balance, int days) {
        jdbc.update("""
                INSERT INTO "timeDeposits" (id, "planType", balance, days) VALUES (?, ?, ?, ?)
                """, id, planType, new BigDecimal(balance), days);
    }

    private void insertWithdrawal(int id, int timeDepositId, String amount, LocalDate date) {
        jdbc.update("""
                INSERT INTO withdrawals (id, "timeDepositId", amount, date) VALUES (?, ?, ?, ?)
                """, id, timeDepositId, new BigDecimal(amount), date);
    }

}
