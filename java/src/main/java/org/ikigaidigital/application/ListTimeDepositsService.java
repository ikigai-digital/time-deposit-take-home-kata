package org.ikigaidigital.application;

import org.ikigaidigital.application.port.in.ListTimeDepositsUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.domain.model.TimeDeposit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListTimeDepositsService implements ListTimeDepositsUseCase {

    private final TimeDepositRepository timeDeposits;

    public ListTimeDepositsService(TimeDepositRepository timeDeposits) {
        this.timeDeposits = timeDeposits;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeDeposit> listAll() {
        return timeDeposits.findAll();
    }
}
