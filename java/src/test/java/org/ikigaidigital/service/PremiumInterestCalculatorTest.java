package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDeposit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PremiumInterestCalculatorTest {

    private final PremiumInterestCalculator calculator = new PremiumInterestCalculator();

    @Test
    void whenDaysGreaterThan45_thenShouldCalculateInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "PREMIUM", 1200.00, 46);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(5.0, interest, 0.0001);
    }

    @Test
    void whenDaysEqualTo45_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "PREMIUM", 1200.00, 45);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }

    @Test
    void whenDaysLessThanOrEqualTo30_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "PREMIUM", 1200.00, 30);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }

    @Test
    void whenBalanceIsZero_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "PREMIUM", 0.0, 50);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }
}

