package org.ikigaidigital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ikigaidigital.mapper.TimeDepositResponseMapper;
import org.ikigaidigital.service.TimeDepositService;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.ikigaidigital.util.TestUtil.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class TimeDepositControllerTest {

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