package org.ikigaidigital.adapter.in.rest;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureMockMvc
@Testcontainers
class TimeDepositApiIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void resetDatabaseToSampleData() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @DisplayName("Should return all time deposits with their withdrawals")
    void shouldReturnAllTimeDepositsWithTheirWithdrawals() throws Exception {
        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[*].id", contains(1, 2, 3, 4, 5, 6)))
                .andExpect(jsonPath("$[*].planType", contains("basic", "basic", "student", "student", "premium", "premium")))
                .andExpect(jsonPath("$[*].days", contains(45, 20, 200, 400, 40, 120)))
                .andExpect(jsonPath("$[0].balance").value(10000.00))
                .andExpect(jsonPath("$[0].withdrawals", hasSize(2)))
                .andExpect(jsonPath("$[0].withdrawals[0].id").value(1))
                .andExpect(jsonPath("$[0].withdrawals[0].amount").value(500.00))
                .andExpect(jsonPath("$[0].withdrawals[0].date").value("2026-10-01"))
                .andExpect(jsonPath("$[1].withdrawals", hasSize(0)))
                .andExpect(jsonPath("$[2].withdrawals", hasSize(1)))
                .andExpect(jsonPath("$[5].withdrawals[0].amount").value(20000.00));
    }

    @Test
    @DisplayName("Should apply one month of interest to every time deposit and persist the balances")
    void shouldApplyOneMonthOfInterestToEveryTimeDeposit() throws Exception {
        mockMvc.perform(post("/time-deposits/balance-updates"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].balance",
                        contains(10008.33, 5000.00, 20050.00, 15000.00, 30000.00, 1239711.03)));
    }

    @Test
    @DisplayName("Should add another month of interest on every balance update call")
    void shouldAddAnotherMonthOfInterestOnEveryCall() throws Exception {
        mockMvc.perform(post("/time-deposits/balance-updates"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/time-deposits/balance-updates"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/time-deposits"))
                .andExpect(jsonPath("$[0].balance").value(10016.67))
                .andExpect(jsonPath("$[5].balance").value(1244876.49));
    }
}
