package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDepositWithWithdrawals;

import java.util.List;

public interface TimeDepositService {
    List<TimeDepositWithWithdrawals> getTimeDeposits();
}
