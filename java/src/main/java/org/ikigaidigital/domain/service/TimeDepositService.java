package org.ikigaidigital.domain.service;

import java.util.List;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.TimeDepositUseCase;
import org.springframework.stereotype.Service;

@Service
public class TimeDepositService implements TimeDepositUseCase {
    private final TimeDepositCalculator calculator;

    public TimeDepositService(TimeDepositCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public List<TimeDeposit> getAllDeposits() {
        return List.of();
    }

    @Override
    public void updateAllBalances() {
        List<TimeDeposit> deposits = getAllDeposits();
        calculator.updateBalance(deposits);
    }
}
