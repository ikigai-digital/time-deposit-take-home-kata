package org.ikigaidigital.application.port.out;

import org.ikigaidigital.TimeDeposit;

import java.util.List;

public interface TimeDepositRepository {

    /** All time deposits, ordered by id. */
    List<TimeDeposit> findAll();

    /** Persists the current state (balance) of the given, already existing time deposits. */
    void saveAll(List<TimeDeposit> timeDeposits);
}
