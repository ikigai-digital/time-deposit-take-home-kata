package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.adapter.out.persistence.entity.TimeDepositEntity;
import org.ikigaidigital.adapter.out.persistence.entity.WithdrawalEntity;
import org.ikigaidigital.domain.model.TimeDeposit;
import org.ikigaidigital.domain.model.Withdrawal;

import java.util.Comparator;
import java.util.List;

final class TimeDepositEntityMapper {

    private TimeDepositEntityMapper() {
    }

    static TimeDeposit toDomain(TimeDepositEntity entity) {
        List<Withdrawal> withdrawals = entity.getWithdrawals().stream()
                .sorted(Comparator.comparing(WithdrawalEntity::getId))
                .map(withdrawal -> new Withdrawal(
                        withdrawal.getId(),
                        entity.getId(),
                        withdrawal.getAmount(),
                        withdrawal.getDate()))
                .toList();
        return new TimeDeposit(
                entity.getId(),
                entity.getPlanType(),
                entity.getBalance(),
                entity.getDays(),
                withdrawals);
    }
}
