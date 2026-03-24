package org.ikigaidigital.application.port.in;

import java.util.List;
import org.ikigaidigital.TimeDeposit;

public interface TimeDepositUseCase {
    List<TimeDeposit> getAllDeposits();
    void updateAllBalances();
}
