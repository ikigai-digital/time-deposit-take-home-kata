package org.ikigaidigital.service;

import com.github.fge.jsonpatch.JsonPatch;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;

import java.util.List;

public interface TimeDepositService {
    List<TimeDepositWithWithdrawals> getTimeDeposits();
    TimeDepositWithWithdrawals update(int id, JsonPatch updates);
}
