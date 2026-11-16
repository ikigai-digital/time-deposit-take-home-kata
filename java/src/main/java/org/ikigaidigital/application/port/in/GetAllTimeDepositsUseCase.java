package org.ikigaidigital.application.port.in;

import org.ikigaidigital.application.TimeDepositWithWithdrawals;

import java.util.List;

public interface GetAllTimeDepositsUseCase {

    List<TimeDepositWithWithdrawals> getAllTimeDeposits();
}
