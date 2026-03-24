package org.ikigaidigital.domain.service;

import java.util.List;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.TimeDepositUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class TimeDepositService implements TimeDepositUseCase {
    private final TimeDepositCalculator calculator;
    private final TimeDepositRepositoryPort repository;

    public TimeDepositService(
        TimeDepositCalculator calculator,
        TimeDepositRepositoryPort repository
    ) {
        this.calculator = calculator;
        this.repository = repository;
    }

    @Override
    public List<TimeDeposit> getAllDeposits() {
        return repository.findAll();
    }

    @Override
    public void updateAllBalances() {
        List<TimeDeposit> deposits = getAllDeposits();
        calculator.updateBalance(deposits);
        repository.saveAll(deposits);
    }
}
