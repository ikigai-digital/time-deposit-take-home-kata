package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Outbound adapter storing {@link TimeDeposit}s in the {@code time_deposits} table.
 * <p>
 * Money: the domain uses {@code Double} (the shared class cannot change), the database NUMERIC(19, 2).
 * Reading converts exactly; writing rounds HALF_UP to cents. The calculator already rounds interest to
 * cents, so in practice the rounding only absorbs binary floating-point noise.
 */
@Component
class TimeDepositPersistenceAdapter implements TimeDepositRepository {

    static final int MONEY_SCALE = 2;

    private final TimeDepositJpaRepository jpaRepository;

    TimeDepositPersistenceAdapter(TimeDepositJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeDeposit> findAll() {
        return jpaRepository.findAll(Sort.by("id")).stream()
                .map(TimeDepositPersistenceAdapter::toDomain)
                .toList();
    }

    /**
     * Only the balance is written: it is the only mutable field of {@link TimeDeposit}. Entities are loaded
     * and updated (not rebuilt from the domain objects), so other columns can never be overwritten.
     * Assumption: deposits passed in exist (they come from {@link #findAll()}); unknown ids are ignored,
     * as handling invalid input is out of scope.
     */
    @Override
    @Transactional
    public void saveAll(List<TimeDeposit> timeDeposits) {
        Map<Integer, TimeDepositEntity> entitiesById = jpaRepository
                .findAllById(timeDeposits.stream().map(TimeDeposit::getId).toList()).stream()
                .collect(toMap(TimeDepositEntity::getId, Function.identity()));

        for (TimeDeposit deposit : timeDeposits) {
            TimeDepositEntity entity = entitiesById.get(deposit.getId());
            if (entity != null) {
                entity.setBalance(toMoney(deposit.getBalance()));
            }
        }
        // Managed entities are flushed on commit (dirty checking); no explicit save needed.
    }

    private static TimeDeposit toDomain(TimeDepositEntity entity) {
        return new TimeDeposit(entity.getId(), entity.getPlanType(), entity.getBalance().doubleValue(),
                entity.getDays());
    }

    /** {@code BigDecimal.valueOf} uses the shortest decimal form of the double (10008.33, not 10008.329999...). */
    static BigDecimal toMoney(double amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
