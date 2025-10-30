package org.ikigaidigital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ikigaidigital.exception.InternalServerErrorException;
import org.ikigaidigital.exception.TimeDepositNotFoundException;
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
import java.util.Map;

import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_RESPONSE_1;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_RESPONSE_2;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_WITH_WITHDRAWALS_1;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_WITH_WITHDRAWALS_2;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@WebMvcTest
class TimeDepositControllerTest {

    private static final String ERROR_MSG_GET_DEPOSITS = "Error while calling: GET /deposits";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeDepositService timeDepositService;

    @MockitoBean
    private TimeDepositResponseMapper timeDepositResponseMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getTimeDeposits_success() throws Exception {

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

    @Test
    void getEmptyResponse() throws Exception {

        when(timeDepositService.getTimeDeposits()).thenReturn(List.of());

        List<TimeDepositResponse> expectedResponse = List.of();
        String expectedResponseAsJson = objectMapper.writeValueAsString(expectedResponse);

        this.mockMvc.perform(get("/deposits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseAsJson));
    }

    @Test
    void getTimeDeposits_whenServiceThrowsException_propagateInternalServerErrorException() throws Exception {

        when(timeDepositService.getTimeDeposits()).thenThrow(new RuntimeException("exception_test_msg"));

        List<TimeDepositResponse> expectedResponse = List.of();
        String expectedResponseAsJson = objectMapper.writeValueAsString(expectedResponse);

        this.mockMvc.perform(get("/deposits"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(ERROR_MSG_GET_DEPOSITS))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InternalServerErrorException));
    }

    @Test
    void getTimeDeposits_whenMapperThrowsException_propagateInternalServerErrorException() throws Exception {

        when(timeDepositService.getTimeDeposits())
                .thenReturn(List.of(TIME_DEPOSIT_WITH_WITHDRAWALS_1, TIME_DEPOSIT_WITH_WITHDRAWALS_2));
        when(timeDepositResponseMapper.map(TIME_DEPOSIT_WITH_WITHDRAWALS_1))
                .thenReturn(TIME_DEPOSIT_RESPONSE_1);
        when(timeDepositResponseMapper.map(TIME_DEPOSIT_WITH_WITHDRAWALS_2))
                .thenThrow(new RuntimeException("exception_test_msg"));

        List<TimeDepositResponse> expectedResponse = List.of();
        String expectedResponseAsJson = objectMapper.writeValueAsString(expectedResponse);

        this.mockMvc.perform(get("/deposits"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(ERROR_MSG_GET_DEPOSITS))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InternalServerErrorException));
    }

    @Test
    void updateBalanceOnTimeDeposit() throws Exception {

        String patchRequestBody = """
                   {
                     "balance": 1500.6
                   }
                """;

        when(timeDepositService.update(1, Map.of("balance", 1500.6)))
                .thenReturn(TIME_DEPOSIT_WITH_WITHDRAWALS_1);
        when(timeDepositResponseMapper.map(TIME_DEPOSIT_WITH_WITHDRAWALS_1))
                .thenReturn(TIME_DEPOSIT_RESPONSE_1);

        String expectedResponseAsJson = objectMapper.writeValueAsString(TIME_DEPOSIT_RESPONSE_1);

        this.mockMvc.perform(patch("/deposits/1")
                        .content(patchRequestBody)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().json(expectedResponseAsJson));
    }

    @Test
    void updateBalanceOnTimeDeposit_timeDepositNotFound_shouldReturn404StatusCode() throws Exception {

        String patchRequestBody = """
                   {
                     "balance": 1500.6
                   }
                """;

        when(timeDepositService.update(1, Map.of("balance", 1500.6)))
                .thenThrow(TimeDepositNotFoundException.class);

        this.mockMvc.perform(patch("/deposits/1")
                        .content(patchRequestBody)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE));
    }

    @Test
    void updateBalanceOnTimeDeposit_exceptionDuringProcessing_shouldReturn500InternalServerError() throws Exception {

        String patchRequestBody = """
                   {
                     "balance": 1500.6
                   }
                """;

        when(timeDepositService.update(1, Map.of("balance", 1500.6)))
                .thenThrow(InternalServerErrorException.class);

        this.mockMvc.perform(patch("/deposits/1")
                        .content(patchRequestBody)
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE));
    }
}