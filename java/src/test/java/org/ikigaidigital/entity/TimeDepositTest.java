package org.ikigaidigital.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TimeDepositTest {

    @Test
    void whenCreateWithAllArgs_thenShouldSetFields() {
        TimeDeposit deposit = new TimeDeposit(1, PlanType.BASIC, 30, new BigDecimal("1000.00"), List.of());

        assertEquals(1, deposit.getId());
        assertEquals(PlanType.BASIC, deposit.getPlanType());
        assertEquals(30, deposit.getDays());
        assertEquals(new BigDecimal("1000.00"), deposit.getBalance());
        assertNotNull(deposit.getWithdrawals());
    }

    @Test
    void whenCreateWithStringPlanTypeConstructor_thenShouldConvertEnum() {
        TimeDeposit deposit = new TimeDeposit("student", 365, new BigDecimal("2000.00"));

        assertEquals(PlanType.STUDENT, deposit.getPlanType());
        assertEquals(365, deposit.getDays());
        assertEquals(new BigDecimal("2000.00"), deposit.getBalance());
    }

    @Test
    void whenNoArgsConstructor_thenShouldHaveNullFields() {
        TimeDeposit deposit = new TimeDeposit();

        assertNull(deposit.getId());
        assertNull(deposit.getPlanType());
        assertNull(deposit.getDays());
        assertNull(deposit.getBalance());
        assertNull(deposit.getWithdrawals());
    }

    @Test
    void whenToString_thenShouldContainFieldValues() {
        TimeDeposit deposit = new TimeDeposit(1, PlanType.PREMIUM, 180, new BigDecimal("5000.00"), List.of());
        String str = deposit.toString();

        assertTrue(str.contains("PREMIUM"));
        assertTrue(str.contains("5000.00"));
        assertTrue(str.contains("180"));
    }

    @Test
    void whenEqualsAndHashCode_thenShouldMatchForSameValues() {
        TimeDeposit d1 = new TimeDeposit(1, PlanType.BASIC, 30, new BigDecimal("1000.00"), List.of());
        TimeDeposit d2 = new TimeDeposit(1, PlanType.BASIC, 30, new BigDecimal("1000.00"), List.of());

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }
}

