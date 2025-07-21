package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDeposit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BasicInterestCalculatorTest {

    private final BasicInterestCalculator calculator = new BasicInterestCalculator();

    @Test
    void whenDaysGreaterThan30_thenShouldCalculateInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "BASIC", 1200.00, 31);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(1.0, interest, 0.0001);
    }

    @Test
    void whenDaysEqualTo30_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "BASIC", 1200.00, 30);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }

    @Test
    void whenDaysLessThan30_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "BASIC", 1200.00, 10);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }

    @Test
    void whenBalanceIsZero_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "BASIC", 0.0, 40);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }
}
