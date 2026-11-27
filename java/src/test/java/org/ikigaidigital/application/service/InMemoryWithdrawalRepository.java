package org.ikigaidigital.application.service;

import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.application.port.out.WithdrawalRepository;

import java.util.List;

class InMemoryWithdrawalRepository implements WithdrawalRepository {

    private final List<Withdrawal> withdrawals;

    InMemoryWithdrawalRepository(Withdrawal... withdrawals) {
        this.withdrawals = List.of(withdrawals);
    }

    @Override
    public List<Withdrawal> findAll() {
        return withdrawals;
    }
}
