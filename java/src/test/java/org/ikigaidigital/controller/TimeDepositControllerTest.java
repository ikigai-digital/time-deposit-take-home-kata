package org.ikigaidigital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ikigaidigital.mapper.TimeDepositResponseMapper;
import org.ikigaidigital.model.PlanType;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.model.Withdrawal;
import org.ikigaidigital.service.TimeDepositService;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;
import org.ikigaidigital.web.domain.response.WithdrawalResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class TimeDepositControllerTest {

    private static final Withdrawal WITHDRAWAL_1 = new Withdrawal(10d, OffsetDateTime.now());
    private static final Withdrawal WITHDRAWAL_2 = new Withdrawal(20d, OffsetDateTime.now());
    private static final Withdrawal WITHDRAWAL_3 = new Withdrawal(30d, OffsetDateTime.now());

    private static final WithdrawalResponse WITHDRAWAL_RESPONSE_1 = new WithdrawalResponse(WITHDRAWAL_1.getAmount(),
            WITHDRAWAL_1.getDate());
    private static final WithdrawalResponse WITHDRAWAL_RESPONSE_2 = new WithdrawalResponse(WITHDRAWAL_2.getAmount(),
            WITHDRAWAL_2.getDate());
    private static final WithdrawalResponse WITHDRAWAL_RESPONSE_3 = new WithdrawalResponse(WITHDRAWAL_3.getAmount(),
            WITHDRAWAL_3.getDate());

    private final static TimeDepositWithWithdrawals TIME_DEPOSIT_WITH_WITHDRAWALS_1 =
            new TimeDepositWithWithdrawals(1, PlanType.BASIC.name(), 100d, 365,
                    List.of(WITHDRAWAL_1));
    private final static TimeDepositWithWithdrawals TIME_DEPOSIT_WITH_WITHDRAWALS_2 =
            new TimeDepositWithWithdrawals(2, PlanType.STUDENT.name(), 200d, 900,
                    List.of(WITHDRAWAL_2, WITHDRAWAL_3));

    private final static TimeDepositResponse TIME_DEPOSIT_RESPONSE_1 =
            new TimeDepositResponse(TIME_DEPOSIT_WITH_WITHDRAWALS_1.getId(), TIME_DEPOSIT_WITH_WITHDRAWALS_1.getPlanType(),
                    TIME_DEPOSIT_WITH_WITHDRAWALS_1.getBalance(), TIME_DEPOSIT_WITH_WITHDRAWALS_1.getDays(),
                    List.of(WITHDRAWAL_RESPONSE_1));
    private final static TimeDepositResponse TIME_DEPOSIT_RESPONSE_2 =
            new TimeDepositResponse(TIME_DEPOSIT_WITH_WITHDRAWALS_2.getId(), TIME_DEPOSIT_WITH_WITHDRAWALS_2.getPlanType(),
                    TIME_DEPOSIT_WITH_WITHDRAWALS_2.getBalance(), TIME_DEPOSIT_WITH_WITHDRAWALS_2.getDays(),
                    List.of(WITHDRAWAL_RESPONSE_2, WITHDRAWAL_RESPONSE_3));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeDepositService timeDepositService;

    @MockitoBean
    private TimeDepositResponseMapper timeDepositResponseMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getTimeDeposits_success() throws Exception {

        when(timeDepositService.getTimeDeposits())
                .thenReturn(List.of(TIME_DEPOSIT_WITH_WITHDRAWALS_1, TIME_DEPOSIT_WITH_WITHDRAWALS_2));
        when(timeDepositResponseMapper.map(TIME_DEPOSIT_WITH_WITHDRAWALS_1))
                .thenReturn(TIME_DEPOSIT_RESPONSE_1);
        when(timeDepositResponseMapper.map(TIME_DEPOSIT_WITH_WITHDRAWALS_2))
                .thenReturn(TIME_DEPOSIT_RESPONSE_2);

        List<TimeDepositResponse> expectedResponse
                = List.of(TIME_DEPOSIT_RESPONSE_1, TIME_DEPOSIT_RESPONSE_2);
        String expectedResponseAsJson = objectMapper.writeValueAsString(expectedResponse);

        this.mockMvc.perform(get("/deposits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseAsJson));
    }
}