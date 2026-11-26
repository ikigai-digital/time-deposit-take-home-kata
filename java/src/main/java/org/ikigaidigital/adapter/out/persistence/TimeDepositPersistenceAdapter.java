package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.domain.model.TimeDeposit;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TimeDepositPersistenceAdapter implements TimeDepositRepository {

    private final TimeDepositJpaRepository timeDeposits;

    public TimeDepositPersistenceAdapter(TimeDepositJpaRepository timeDeposits) {
        this.timeDeposits = timeDeposits;
    }

    @Override
    public List<TimeDeposit> findAll() {
        return timeDeposits.findAllByOrderByIdAsc().stream()
                .map(TimeDepositEntityMapper::toDomain)
                .toList();
    }
}
