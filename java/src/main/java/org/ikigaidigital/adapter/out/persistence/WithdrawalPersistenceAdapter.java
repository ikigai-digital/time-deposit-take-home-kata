package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.application.port.out.WithdrawalRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Outbound adapter reading {@link Withdrawal}s from the {@code withdrawals} table. */
@Component
class WithdrawalPersistenceAdapter implements WithdrawalRepository {

    private static final Sort BY_DATE_THEN_ID = Sort.by("date", "id");

    private final WithdrawalJpaRepository jpaRepository;

    WithdrawalPersistenceAdapter(WithdrawalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Withdrawal> findAll() {
        return jpaRepository.findAll(BY_DATE_THEN_ID).stream()
                .map(WithdrawalPersistenceAdapter::toDomain)
                .toList();
    }

    private static Withdrawal toDomain(WithdrawalEntity entity) {
        return new Withdrawal(entity.getId(), entity.getTimeDepositId(), entity.getAmount(), entity.getDate());
    }
}
