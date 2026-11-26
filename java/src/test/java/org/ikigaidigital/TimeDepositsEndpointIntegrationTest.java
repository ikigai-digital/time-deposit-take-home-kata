package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TimeDepositsEndpointIntegrationTest extends TimeDepositDatabase {

    @DynamicPropertySource
    static void dataSource(DynamicPropertyRegistry registry) {
        POSTGRES.start();
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsTheSampleTimeDeposits() throws Exception {
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
                                "id": 1,
                                "timeDepositId": 1,
                                "amount": "100.50",
                                "date": "2026-01-15"
                              },
                              {
                                "id": 2,
                                "timeDepositId": 1,
                                "amount": "25.00",
                                "date": "2026-02-01"
                              }
                            ]
                          },
                          {
                            "id": 2,
                            "planType": "student",
                            "balance": "8000.00",
                            "days": 200,
                            "withdrawals": []
                          },
                          {
                            "id": 3,
                            "planType": "premium",
                            "balance": "25000.50",
                            "days": 60,
                            "withdrawals": [
                              {
                                "id": 3,
                                "timeDepositId": 3,
                                "amount": "500.00",
                                "date": "2026-03-10"
                              }
                            ]
                          }
                        ]
                        """, true));
    }
}
