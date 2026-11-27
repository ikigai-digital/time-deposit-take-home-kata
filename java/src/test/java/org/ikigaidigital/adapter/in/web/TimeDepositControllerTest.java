package org.ikigaidigital.adapter.in.web;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.TimeDepositDetails;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web slice: HTTP mapping and JSON schema, with the use cases mocked. */
@WebMvcTest(TimeDepositController.class)
class TimeDepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAllTimeDepositsUseCase getAllTimeDeposits;

    @MockitoBean
    private UpdateAllBalancesUseCase updateAllBalances;

    @Test
    void returnsAllTimeDepositsWithWithdrawals() throws Exception {
        given(getAllTimeDeposits.getAllTimeDeposits()).willReturn(List.of(
                new TimeDepositDetails(new TimeDeposit(1, "basic", 10_008.33, 45), List.of(
                        new Withdrawal(7, 1, new BigDecimal("500.00"), LocalDate.of(2026, 1, 15)))),
                new TimeDepositDetails(new TimeDeposit(2, "premium", 20_000.00, 90), List.of())));

        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"id": 1, "planType": "basic", "balance": 10008.33, "days": 45,
                           "withdrawals": [{"id": 7, "amount": 500.00, "date": "2026-01-15"}]},
                          {"id": 2, "planType": "premium", "balance": 20000.00, "days": 90, "withdrawals": []}
                        ]
                        """, true));
    }

    @Test
    void exposesMoneyWithTwoDecimalPlaces() throws Exception {
        // 0.1 + 0.2 = 0.30000000000000004 as a double
        given(getAllTimeDeposits.getAllTimeDeposits()).willReturn(List.of(
                new TimeDepositDetails(new TimeDeposit(1, "basic", 0.1 + 0.2, 45), List.of())));

        mockMvc.perform(get("/time-deposits"))
                .andExpect(content().string(containsString("\"balance\":0.30")));
    }

    @Test
    void updatesAllBalances() throws Exception {
        mockMvc.perform(post("/time-deposits/balance-updates"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(updateAllBalances).updateAllBalances();
    }

    @Test
    void doesNotUpdateBalancesOnGet() throws Exception {
        mockMvc.perform(get("/time-deposits/balance-updates")).andExpect(status().is4xxClientError());

        verifyNoInteractions(updateAllBalances);
    }
}
