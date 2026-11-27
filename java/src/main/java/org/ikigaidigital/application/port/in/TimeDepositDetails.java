package org.ikigaidigital.application.port.in;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;

import java.util.List;

/**
 * Read model returned by {@link GetAllTimeDepositsUseCase}: a time deposit together with its withdrawals.
 */
public record TimeDepositDetails(TimeDeposit timeDeposit, List<Withdrawal> withdrawals) {

    public TimeDepositDetails {
        withdrawals = List.copyOf(withdrawals);
    }
}
