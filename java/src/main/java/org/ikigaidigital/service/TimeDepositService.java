package org.ikigaidigital.service;

import org.ikigaidigital.model.TimeDepositWithWithdrawals;

import java.util.List;
import java.util.Map;

public interface TimeDepositService {
    List<TimeDepositWithWithdrawals> getTimeDeposits();
    TimeDepositWithWithdrawals update(int id, final Map<String, Object> updates);
}
