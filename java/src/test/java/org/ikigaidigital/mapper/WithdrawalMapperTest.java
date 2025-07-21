package org.ikigaidigital.mapper;

import org.ikigaidigital.entity.Withdrawal;
import org.ikigaidigital.model.WithdrawalResponse;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WithdrawalMapperTest {

    private final WithdrawalMapper mapper = new WithdrawalMapper();

    @Test
    void whenWithdrawalListIsNull_thenShouldReturnEmptyList() {
        List<WithdrawalResponse> result = mapper.toWithdrawalResponse(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenWithdrawalListIsEmpty_thenShouldReturnEmptyList() {
        List<WithdrawalResponse> result = mapper.toWithdrawalResponse(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenWithdrawalListHasValues_thenShouldMapToResponse() {
        Withdrawal withdrawal = Withdrawal.builder()
            .id(1)
            .amount(new BigDecimal("100.00"))
            .date(LocalDate.of(2025, 7, 20))
            .build();
        List<WithdrawalResponse> result = mapper.toWithdrawalResponse(List.of(withdrawal));

        assertEquals(1, result.size());
        WithdrawalResponse response = result.get(0);
        assertEquals("1", response.id());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals("2025-07-20", response.date());
    }
}

