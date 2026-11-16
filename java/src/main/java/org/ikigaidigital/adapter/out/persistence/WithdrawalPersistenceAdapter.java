package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.application.port.out.WithdrawalRepository;
import org.ikigaidigital.domain.Withdrawal;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class WithdrawalPersistenceAdapter implements WithdrawalRepository {

    private static final Sort BY_DATE_AND_ID = Sort.by("date", "id");

    private final WithdrawalJpaRepository withdrawalJpaRepository;

    WithdrawalPersistenceAdapter(WithdrawalJpaRepository withdrawalJpaRepository) {
        this.withdrawalJpaRepository = withdrawalJpaRepository;
    }

    @Override
    public List<Withdrawal> findAll() {
        return withdrawalJpaRepository.findAll(BY_DATE_AND_ID).stream()
                .map(this::toWithdrawal)
                .toList();
    }

    private Withdrawal toWithdrawal(WithdrawalEntity withdrawalEntity) {
        return new Withdrawal(
                withdrawalEntity.getId(),
                withdrawalEntity.getTimeDepositId(),
                withdrawalEntity.getAmount(),
                withdrawalEntity.getDate());
    }
}
