package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class UpdateAllBalancesService implements UpdateAllBalancesUseCase {

    private final TimeDepositRepository timeDepositRepository;
    private final TimeDepositCalculator calculator;

    public UpdateAllBalancesService(TimeDepositRepository timeDepositRepository, TimeDepositCalculator calculator) {
        this.timeDepositRepository = timeDepositRepository;
        this.calculator = calculator;
    }

    /** All deposits are updated in one transaction: either every balance changes or none does. */
    @Override
    @Transactional
    public void updateAllBalances() {
        List<TimeDeposit> timeDeposits = timeDepositRepository.findAll();
        calculator.updateBalance(timeDeposits);
        timeDepositRepository.saveAll(timeDeposits);
    }
}
