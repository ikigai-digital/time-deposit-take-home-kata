package org.ikigaidigital.service.impl;

import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.service.TimeDepositService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeDepositServiceImpl implements TimeDepositService {

    @Override
    public List<TimeDepositWithWithdrawals> getTimeDeposits() {
        return List.of();
    }
}
