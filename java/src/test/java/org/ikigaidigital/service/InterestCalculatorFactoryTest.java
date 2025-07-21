package org.ikigaidigital.service;

import org.ikigaidigital.entity.PlanType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterestCalculatorFactoryTest {

    @Test
    void whenPlanTypeIsBasic_thenShouldReturnBasicInterestCalculator() {
        InterestCalculator calculator = InterestCalculatorFactory.getCalculator(PlanType.BASIC);
        assertInstanceOf(BasicInterestCalculator.class, calculator);
    }

    @Test
    void whenPlanTypeIsStudent_thenShouldReturnStudentInterestCalculator() {
        InterestCalculator calculator = InterestCalculatorFactory.getCalculator(PlanType.STUDENT);
        assertInstanceOf(StudentInterestCalculator.class, calculator);
    }

    @Test
    void whenPlanTypeIsPremium_thenShouldReturnPremiumInterestCalculator() {
        InterestCalculator calculator = InterestCalculatorFactory.getCalculator(PlanType.PREMIUM);
        assertInstanceOf(PremiumInterestCalculator.class, calculator);
    }
}

