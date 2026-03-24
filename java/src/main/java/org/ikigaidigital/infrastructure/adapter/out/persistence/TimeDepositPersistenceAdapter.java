package org.ikigaidigital.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepositoryPort;
import org.ikigaidigital.domain.model.Withdrawal;
import org.springframework.stereotype.Component;

@Component
public class TimeDepositPersistenceAdapter implements TimeDepositRepositoryPort {

    private final SpringDataTimeDepositRepository repository;

    public TimeDepositPersistenceAdapter(SpringDataTimeDepositRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TimeDeposit> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(List<TimeDeposit> deposits) {
        List<TimeDepositEntity> entities = deposits.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }

    private TimeDeposit toDomain(TimeDepositEntity entity) {
        TimeDeposit domain = new TimeDeposit(
            entity.getId(),
            entity.getPlanType(),
            entity.getBalance().doubleValue(),
            entity.getDays());
        domain.setWithdrawals(entity.getWithdrawals().stream()
                .map(w -> new Withdrawal(w.getId(), w.getAmount(), w.getDate()))
                .collect(Collectors.toList()));
        return domain;
    }

    private TimeDepositEntity toEntity(TimeDeposit domain) {
        TimeDepositEntity entity = new TimeDepositEntity();
        entity.setId(domain.getId());
        entity.setPlanType(domain.getPlanType());
        entity.setBalance(
            BigDecimal.valueOf(domain.getBalance()).setScale(2, RoundingMode.HALF_UP)
        );
        entity.setDays(domain.getDays());
        return entity;
    }
}
