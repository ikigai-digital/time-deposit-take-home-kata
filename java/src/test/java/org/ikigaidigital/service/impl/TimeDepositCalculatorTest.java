package org.ikigaidigital.service.impl;

import org.ikigaidigital.model.PlanType;
import org.ikigaidigital.model.TimeDeposit;
import org.ikigaidigital.service.TimeDepositCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ikigaidigital.model.PlanType.BASIC;
import static org.ikigaidigital.model.PlanType.PREMIUM;
import static org.ikigaidigital.model.PlanType.STUDENT;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TimeDepositCalculatorTest {

    @Mock
    private StudentInterestCalculator studentInterestCalculator;

    @Mock
    private PremiumInterestCalculator premiumInterestCalculator;

    @Mock
    private BasicInterestCalculator basicInterestCalculator;

    @ParameterizedTest
    @EnumSource(PlanType.class)
    void updateBalance_planDaysLowerOrEqualTo45_successfully(PlanType planType) {

        TimeDepositCalculator calc = new TimeDepositCalculator(studentInterestCalculator,
                premiumInterestCalculator, basicInterestCalculator);

        List<TimeDeposit> plans = Arrays.asList(
                new TimeDeposit(1, planType.name(), 1234567.00, 45)
        );

        calc.updateBalance(plans);

        assertThat(plans.get(0).getBalance()).isEqualTo(1234567.0);
    }

    @Test
    void updateBalance_planTypeStudent() {

        TimeDepositCalculator calc = new TimeDepositCalculator(studentInterestCalculator,
                premiumInterestCalculator, basicInterestCalculator);

        TimeDeposit timeDeposit = new TimeDeposit(1, STUDENT.name(), 1234567.00, 120);

        when(studentInterestCalculator.calculate(timeDeposit)).thenReturn(100.00);

        calc.updateBalance(List.of(timeDeposit));

        assertThat(timeDeposit.getBalance()).isEqualTo(1234667.0);

        verifyNoInteractions(premiumInterestCalculator, basicInterestCalculator);
    }

    @Test
    void updateBalance_planTypePremium() {

        TimeDepositCalculator calc = new TimeDepositCalculator(studentInterestCalculator,
                premiumInterestCalculator, basicInterestCalculator);

        TimeDeposit timeDeposit = new TimeDeposit(1, PREMIUM.name(), 1234567.00, 120);

        when(premiumInterestCalculator.calculate(timeDeposit)).thenReturn(1230.00);

        calc.updateBalance(List.of(timeDeposit));

        assertThat(timeDeposit.getBalance()).isEqualTo(1235797.0);

        verifyNoInteractions(studentInterestCalculator, basicInterestCalculator);

    }

    @Test
    void updateBalance_planTypeBasic() {

        TimeDepositCalculator calc = new TimeDepositCalculator(studentInterestCalculator,
                premiumInterestCalculator, basicInterestCalculator);

        TimeDeposit timeDeposit = new TimeDeposit(1, BASIC.name(), 1234567.00, 120);

        when(basicInterestCalculator.calculate(timeDeposit)).thenReturn(4243523.98);

        calc.updateBalance(List.of(timeDeposit));

        assertThat(timeDeposit.getBalance()).isEqualTo(1238103.27);

        verifyNoInteractions(studentInterestCalculator, premiumInterestCalculator);

    }

}
