package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDeposit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentInterestCalculatorTest {

    private final StudentInterestCalculator calculator = new StudentInterestCalculator();

    @Test
    void whenDaysGreaterThan30AndLessThan366_thenShouldCalculateInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "STUDENT", 1200.00, 100);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(3.0, interest, 0.0001); // 1200 * 0.03 / 12 = 3.0
    }

    @Test
    void whenDaysEqualTo30_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "STUDENT", 1200.00, 30);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }

    @Test
    void whenDaysEqualTo366_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "STUDENT", 1200.00, 366);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }

    @Test
    void whenDaysLessThanOrEqualTo30_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "STUDENT", 1200.00, 10);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }

    @Test
    void whenBalanceIsZero_thenShouldReturnZeroInterest() {
        TimeDeposit deposit = new TimeDeposit(1, "STUDENT", 0.0, 100);
        double interest = calculator.calculateInterest(deposit);
        assertEquals(0.0, interest, 0.0001);
    }
}

