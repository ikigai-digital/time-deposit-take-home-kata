package org.ikigaidigital.application.port.out;

import org.ikigaidigital.domain.model.TimeDeposit;

import java.util.List;

public interface TimeDepositRepository {

    List<TimeDeposit> findAll();
}
