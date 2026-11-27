package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End to end through HTTP, the application core and PostgreSQL, using the sample data (db/sample-data).
 * MockMvc runs on the test thread, so the request joins the test transaction and is rolled back:
 * other tests still see the original sample balances.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestcontainer.class)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class TimeDepositApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSampleTimeDepositsWithTheirWithdrawals() throws Exception {
        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[*].id").value(contains(1, 2, 3, 4, 5, 6, 7)))
                .andExpect(jsonPath("$[0]").value(equalTo(Map.of(
                        "id", 1, "planType", "basic", "balance", 10000.0, "days", 45,
                        "withdrawals", List.of(
                                Map.of("id", 1, "amount", 500.0, "date", "2026-01-15"),
                                Map.of("id", 2, "amount", 250.5, "date", "2026-03-02"))))))
                .andExpect(jsonPath("$[1].withdrawals").isEmpty());
    }

    @Test
    void appliesOneMonthOfInterestToEveryDeposit() throws Exception {
        mockMvc.perform(post("/time-deposits/balance-updates")).andExpect(status().isNoContent());

        // Expected balances are documented next to the sample data.
        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"id": 1, "balance": 10008.33},
                          {"id": 2, "balance": 10000.00},
                          {"id": 3, "balance": 5012.50},
                          {"id": 4, "balance": 5000.00},
                          {"id": 5, "balance": 20000.00},
                          {"id": 6, "balance": 20083.33},
                          {"id": 7, "balance": 1000.00}
                        ]
                        """));
    }

    @Test
    void eachUpdateAccruesAnotherMonth() throws Exception {
        mockMvc.perform(post("/time-deposits/balance-updates")).andExpect(status().isNoContent());
        mockMvc.perform(post("/time-deposits/balance-updates")).andExpect(status().isNoContent());

        // 10008.33 + round(10008.33 * 0.01 / 12) = 10008.33 + 8.34
        mockMvc.perform(get("/time-deposits"))
                .andExpect(jsonPath("$[0].balance").value(10016.67));
    }

    @Test
    void openApiContractHasExactlyTheTwoRequiredEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths", aMapWithSize(2)))
                .andExpect(jsonPath("$.paths['/time-deposits']", hasKey("get")))
                .andExpect(jsonPath("$.paths['/time-deposits']", aMapWithSize(1)))
                .andExpect(jsonPath("$.paths['/time-deposits/balance-updates']", hasKey("post")))
                .andExpect(jsonPath("$.paths['/time-deposits/balance-updates']", aMapWithSize(1)))
                .andExpect(jsonPath("$.components.schemas.TimeDeposit.properties",
                        aMapWithSize(5)));
    }
}
