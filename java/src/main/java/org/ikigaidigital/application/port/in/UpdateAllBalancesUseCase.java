package org.ikigaidigital.application.port.in;

/**
 * Applies one month of interest to every stored time deposit.
 * Not idempotent: each call accrues another month.
 */
public interface UpdateAllBalancesUseCase {

    void updateAllBalances();
}
