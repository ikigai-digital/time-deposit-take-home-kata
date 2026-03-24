package org.ikigaidigital.application.port.out;

import java.util.List;
import org.ikigaidigital.TimeDeposit;

public interface TimeDepositRepositoryPort {
    List<TimeDeposit> findAll();
    void saveAll(List<TimeDeposit> deposits);
}
