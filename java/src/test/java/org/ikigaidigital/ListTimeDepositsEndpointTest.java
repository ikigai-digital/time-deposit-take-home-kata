package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ListTimeDepositsEndpointTest extends TimeDepositDatabase {

    @DynamicPropertySource
    static void dataSource(DynamicPropertyRegistry registry) {
        POSTGRES.start();
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void returnsAnEmptyListWhenNothingIsStored() throws Exception {
        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void returnsDepositsWithTheirWithdrawals() throws Exception {
        insertDeposit(1, "basic", 45, new BigDecimal("1234567.00"));
        insertDeposit(2, "student", 10, new BigDecimal("10.00"));
        insertWithdrawal(10, 1, new BigDecimal("100.50"), LocalDate.of(2026, 1, 15));

        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "id": 1,
                            "planType": "basic",
                            "balance": "1234567.00",
                            "days": 45,
                            "withdrawals": [
                              {
                                "id": 10,
                                "timeDepositId": 1,
                                "amount": "100.50",
                                "date": "2026-01-15"
                              }
                            ]
                          },
                          {
                            "id": 2,
                            "planType": "student",
                            "balance": "10.00",
                            "days": 10,
                            "withdrawals": []
                          }
                        ]
                        """, true));
    }

    private void insertDeposit(int id, String planType, int days, BigDecimal balance) {
        jdbc.update(
                "INSERT INTO \"timeDeposits\" (id, \"planType\", days, balance) VALUES (?, ?, ?, ?)",
                id, planType, days, balance);
    }

    private void insertWithdrawal(int id, int timeDepositId, BigDecimal amount, LocalDate date) {
        jdbc.update(
                "INSERT INTO withdrawals (id, \"timeDepositId\", amount, date) VALUES (?, ?, ?, ?)",
                id, timeDepositId, amount, date);
    }
}
