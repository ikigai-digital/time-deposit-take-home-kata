package org.ikigaidigital.timedeposit.api;

import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.assertj.core.api.WithAssertions;
import org.ikigaidigital.timedeposit.TestcontainersConfiguration;
import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
import org.ikigaidigital.timedeposit.domain.TimeDepositCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"balance-update.chunk-size=100", "balance-update.threads=4"})
@Import(TestcontainersConfiguration.class)
class TimeDepositControllerTest implements WithAssertions {

  private static final String BALANCE_UPDATES_PATH = "/api/time-deposits/balance-updates";
  private static final Duration TIMEOUT = Duration.ofSeconds(30);
  private static final Pattern PROCESS_ID = Pattern.compile("\"processId\"\\s*:\\s*(\\d+)");
  private static final String COMPLETED = "COMPLETED";

  private final HttpClient http = HttpClient.newHttpClient();

  @Autowired
  private JdbcClient jdbc;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private TimeDepositCalculator calculator;

  @Autowired
  private Environment environment;

  @BeforeEach
  void cleanDatabase() {
    jdbc.sql("TRUNCATE withdrawals, time_deposits, balance_update_processes RESTART IDENTITY").update();
  }

  @Test
  void shouldStartUpdateAndApplyMonthlyInterestToAllDeposits() throws Exception {
    // given
    seedDeposits(2_345);                          // 2,111 deposits (every 10th id missing) -> 22 chunks
    List<TimeDeposit> expected = loadDeposits();
    calculator.updateBalance(expected);           // what one monthly update must produce

    // when
    HttpResponse<String> response = postBalanceUpdate();

    // then
    assertThat(response.statusCode()).isEqualTo(202);
    assertThat(response.body()).contains("\"status\":\"IN_PROGRESS\"", "Balance update started");
    long processId = processIdFrom(response);
    awaitStatus(processId, COMPLETED);
    assertBalances(loadDeposits(), expected);
  }

  @Test
  void shouldReturnConflictWhenUpdateIsAlreadyInProgress() throws Exception {
    // given
    seedDeposits(500);
    long processId;

    try (Connection lock = lockDeposit(1)) {      // keeps the running update busy on deposit 1
      HttpResponse<String> first = postBalanceUpdate();
      processId = processIdFrom(first);

      // when
      HttpResponse<String> second = postBalanceUpdate();

      // then
      assertThat(first.statusCode()).isEqualTo(202);
      assertThat(second.statusCode()).isEqualTo(409);
      assertThat(second.body()).contains("\"status\":\"IN_PROGRESS\"", "Balance update is already in progress");
      assertThat(statusOf(processId)).isEqualTo("IN_PROGRESS");
      assertThat(processCount()).isEqualTo(1);

      lock.rollback();                            // release the row lock, let the update finish
    }
    awaitStatus(processId, COMPLETED);
  }

  @Test
  void shouldStartNewUpdateAfterPreviousOneFinished() throws Exception {
    // given
    seedDeposits(300);
    long firstProcessId = processIdFrom(postBalanceUpdate());
    awaitStatus(firstProcessId, COMPLETED);
    List<TimeDeposit> expected = loadDeposits();  // balances after the 1st run, as stored (2 decimals)
    calculator.updateBalance(expected);           // the 2nd run must add interest once more

    // when
    HttpResponse<String> response = postBalanceUpdate();

    // then
    assertThat(response.statusCode()).isEqualTo(202);
    long secondProcessId = processIdFrom(response);
    assertThat(secondProcessId).isNotEqualTo(firstProcessId);
    awaitStatus(secondProcessId, COMPLETED);
    assertBalances(loadDeposits(), expected);
  }

  @Test
  void shouldCompleteUpdateWhenThereAreNoDeposits() throws Exception {
    // given empty table

    // when
    HttpResponse<String> response = postBalanceUpdate();

    // then
    assertThat(response.statusCode()).isEqualTo(202);
    awaitStatus(processIdFrom(response), COMPLETED);
  }


  private HttpResponse<String> postBalanceUpdate() throws Exception {
    String port = environment.getRequiredProperty("local.server.port");
    String contextPath = environment.getProperty("server.servlet.context-path", "");
    HttpRequest request = HttpRequest.newBuilder(
            URI.create("http://localhost:" + port + contextPath + BALANCE_UPDATES_PATH))
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();
    return http.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private long processIdFrom(HttpResponse<String> response) {
    Matcher matcher = PROCESS_ID.matcher(response.body());
    assertThat(matcher.find()).as("processId in %s", response.body()).isTrue();
    return Long.parseLong(matcher.group(1));
  }

  /** Mixed plans and durations; every 10th id is left out so chunks do not line up with ids. */
  private void seedDeposits(int maxId) {
    jdbc.sql("""
            INSERT INTO time_deposits (id, plan_type, days, balance)
            SELECT g,
                   (ARRAY ['basic', 'student', 'premium'])[1 + (g % 3)],
                   (g * 7) % 730,
                   round((1000 + (g % 500) * 37.5)::numeric, 2)
              FROM generate_series(1, :maxId) AS g
             WHERE g % 10 <> 0
            """)
        .param("maxId", maxId)
        .update();
  }

  private List<TimeDeposit> loadDeposits() {
    return jdbc.sql("SELECT id, plan_type, balance, days FROM time_deposits ORDER BY id")
        .query((rs, rowNum) -> new TimeDeposit(rs.getInt("id"), rs.getString("plan_type"),
            rs.getBigDecimal("balance").doubleValue(), rs.getInt("days")))
        .list();
  }

  private void assertBalances(List<TimeDeposit> actual, List<TimeDeposit> expected) {
    assertThat(actual).hasSameSizeAs(expected);
    for (int i = 0; i < expected.size(); i++) {
      assertThat(BigDecimal.valueOf(actual.get(i).getBalance()))
          .as("balance of deposit %d", expected.get(i).getId())
          .isEqualByComparingTo(
              BigDecimal.valueOf(expected.get(i).getBalance()).setScale(2, RoundingMode.HALF_UP));
    }
  }

  /** Locks a deposit row in a separate, open transaction: the chunk containing it waits until rollback. */
  private Connection lockDeposit(int id) throws SQLException {
    Connection connection = dataSource.getConnection();
    connection.setAutoCommit(false);
    try (PreparedStatement statement =
        connection.prepareStatement("SELECT id FROM time_deposits WHERE id = ? FOR UPDATE")) {
      statement.setInt(1, id);
      statement.executeQuery();
    }
    return connection;
  }

  private void awaitStatus(long processId, String status) {
    await().atMost(TIMEOUT).until(() -> status.equals(statusOf(processId)));
  }

  private String statusOf(long processId) {
    return jdbc.sql("SELECT status FROM balance_update_processes WHERE id = :id")
        .param("id", processId)
        .query(String.class)
        .single();
  }

  private long processCount() {
    return jdbc.sql("SELECT count(*) FROM balance_update_processes").query(Long.class).single();
  }
}
