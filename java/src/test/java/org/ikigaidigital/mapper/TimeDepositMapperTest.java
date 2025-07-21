package org.ikigaidigital.mapper;

import org.ikigaidigital.entity.PlanType;
import org.ikigaidigital.entity.TimeDeposit;
import org.ikigaidigital.model.TimeDepositResponse;
import org.ikigaidigital.model.WithdrawalResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TimeDepositMapperTest {

    @Mock
    private WithdrawalMapper withdrawalMapper;

    private TimeDepositMapper timeDepositMapper;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        timeDepositMapper = new TimeDepositMapper(withdrawalMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void whenTimeDepositsIsNull_thenShouldReturnEmptyList() {
        List<TimeDepositResponse> result = timeDepositMapper.fromEntityToApiResponse(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenTimeDepositsIsEmpty_thenShouldReturnEmptyList() {
        List<TimeDepositResponse> result = timeDepositMapper.fromEntityToApiResponse(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenTimeDepositsHasValues_thenShouldMapToResponse() {
        TimeDeposit deposit = new TimeDeposit(1, PlanType.BASIC, 30, new BigDecimal("1000.00"), List.of());
        WithdrawalResponse withdrawalResponse = new WithdrawalResponse("1", new BigDecimal("100.00"), "2025-07-20");
        when(withdrawalMapper.toWithdrawalResponse(any())).thenReturn(List.of(withdrawalResponse));
        List<TimeDepositResponse> result = timeDepositMapper.fromEntityToApiResponse(List.of(deposit));

        assertEquals(1, result.size());
        TimeDepositResponse response = result.get(0);
        assertEquals("1", response.id());
        assertEquals("BASIC", response.planType());
        assertEquals(new BigDecimal("1000.00"), response.balance());
        assertEquals(30, response.days());
        assertEquals(List.of(withdrawalResponse), response.withdrawals());
    }

    @Test
    void whenTimeDepositsHasNullWithdrawals_thenShouldMapToResponseWithEmptyWithdrawals() {
        TimeDeposit deposit = new TimeDeposit(1, PlanType.BASIC, 30, new BigDecimal("1000.00"), null);
        when(withdrawalMapper.toWithdrawalResponse(null)).thenReturn(List.of());
        List<TimeDepositResponse> result = timeDepositMapper.fromEntityToApiResponse(List.of(deposit));

        assertEquals(1, result.size());
        TimeDepositResponse response = result.get(0);
        assertEquals("1", response.id());
        assertEquals("BASIC", response.planType());
        assertEquals(new BigDecimal("1000.00"), response.balance());
        assertEquals(30, response.days());
        assertTrue(response.withdrawals().isEmpty());
    }

    @Test
    void whenWithdrawalMapperThrowsException_thenShouldPropagateException() {
        TimeDeposit deposit = new TimeDeposit(1, PlanType.BASIC, 30, new BigDecimal("1000.00"), List.of());
        when(withdrawalMapper.toWithdrawalResponse(any())).thenThrow(new RuntimeException("Withdrawal mapping error"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            timeDepositMapper.fromEntityToApiResponse(List.of(deposit));
        });

        assertEquals("Withdrawal mapping error", thrown.getMessage());
    }
}
