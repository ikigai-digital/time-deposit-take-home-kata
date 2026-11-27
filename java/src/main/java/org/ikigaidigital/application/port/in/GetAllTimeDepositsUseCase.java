package org.ikigaidigital.application.port.in;

import java.util.List;

public interface GetAllTimeDepositsUseCase {

    List<TimeDepositDetails> getAllTimeDeposits();
}
