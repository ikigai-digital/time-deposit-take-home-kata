package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
class TimeDepositPersistenceAdapter implements TimeDepositRepository {

    private static final int BALANCE_SCALE = 2;
    private static final Sort BY_ID = Sort.by("id");

    private final TimeDepositJpaRepository timeDepositJpaRepository;

    TimeDepositPersistenceAdapter(TimeDepositJpaRepository timeDepositJpaRepository) {
        this.timeDepositJpaRepository = timeDepositJpaRepository;
    }

    @Override
    public List<TimeDeposit> findAll() {
        return timeDepositJpaRepository.findAll(BY_ID).stream()
                .map(this::toTimeDeposit)
                .toList();
    }

    @Override
    public void updateBalances(List<TimeDeposit> timeDeposits) {
        for (TimeDeposit timeDeposit : timeDeposits) {
            TimeDepositEntity timeDepositEntity = timeDepositJpaRepository.getReferenceById(timeDeposit.getId());
            timeDepositEntity.setBalance(toDatabaseBalance(timeDeposit.getBalance()));
        }
    }

    private TimeDeposit toTimeDeposit(TimeDepositEntity timeDepositEntity) {
        return new TimeDeposit(
                timeDepositEntity.getId(),
                timeDepositEntity.getPlanType(),
                timeDepositEntity.getBalance().doubleValue(),
                timeDepositEntity.getDays());
    }

    private BigDecimal toDatabaseBalance(Double balance) {
        return BigDecimal.valueOf(balance).setScale(BALANCE_SCALE, RoundingMode.HALF_UP);
    }
}
