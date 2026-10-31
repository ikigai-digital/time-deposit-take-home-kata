package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;

import java.util.List;

/**
 * Defines the storage operations needed by the application without exposing SQL or JDBC.
 * The service owns the transaction; a persistence adapter will implement these operations.
 */
public interface TimeDepositStore {
    /**
     * Returns all deposits and their withdrawals, each ordered by ID.
     * Returns empty lists for no deposits or no withdrawals on a deposit.
     */
    List<TimeDepositView> findAll();

    /** Returns all deposits ordered by ID as mutable calculation inputs. */
    List<TimeDeposit> findAllForCalculation();

    /** Persists only balances by deposit ID, leaving other fields and withdrawals unchanged. */
    void updateBalances(List<TimeDeposit> deposits);
}
