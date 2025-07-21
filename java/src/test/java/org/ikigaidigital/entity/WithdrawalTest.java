package org.ikigaidigital.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class WithdrawalTest {

    @Test
    void whenCreateWithAllArgs_thenShouldSetFields() {
        Withdrawal withdrawal = Withdrawal.builder()
            .id(1)
            .timeDepositId(2)
            .amount(new BigDecimal("100.00"))
            .date(LocalDate.of(2025, 7, 20))
            .build();

        assertEquals(1, withdrawal.getId());
        assertEquals(2, withdrawal.getTimeDepositId());
        assertEquals(new BigDecimal("100.00"), withdrawal.getAmount());
        assertEquals(LocalDate.of(2025, 7, 20), withdrawal.getDate());
    }

    @Test
    void whenNoArgsConstructor_thenShouldHaveNullFields() {
        Withdrawal withdrawal = new Withdrawal();

        assertNull(withdrawal.getId());
        assertNull(withdrawal.getTimeDepositId());
        assertNull(withdrawal.getAmount());
        assertNull(withdrawal.getDate());
        assertNull(withdrawal.getTimeDeposit());
    }

    @Test
    void whenToString_thenShouldContainFieldValues() {
        Withdrawal withdrawal = Withdrawal.builder()
            .id(1)
            .timeDepositId(2)
            .amount(new BigDecimal("100.00"))
            .date(LocalDate.of(2025, 7, 20))
            .build();
        String str = withdrawal.toString();

        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("100.00"));
        assertTrue(str.contains("2025-07-20"));
    }

    @Test
    void whenEqualsAndHashCode_thenShouldMatchForSameValues() {
        Withdrawal w1 = Withdrawal.builder()
            .id(1)
            .timeDepositId(2)
            .amount(new BigDecimal("100.00"))
            .date(LocalDate.of(2025, 7, 20))
            .build();
        Withdrawal w2 = Withdrawal.builder()
            .id(1)
            .timeDepositId(2)
            .amount(new BigDecimal("100.00"))
            .date(LocalDate.of(2025, 7, 20))
            .build();

        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }
}

