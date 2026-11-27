package org.ikigaidigital.application.port.out;

import org.ikigaidigital.Withdrawal;

import java.util.List;

public interface WithdrawalRepository {

    /** All withdrawals, ordered by date, then id. */
    List<Withdrawal> findAll();
}
