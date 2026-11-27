package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.out.TimeDepositRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fake that behaves like a real store: it keeps its own copies, so callers' changes are only
 * visible after {@link #saveAll(List)}, as with a database.
 */
class InMemoryTimeDepositRepository implements TimeDepositRepository {

    private final Map<Integer, TimeDeposit> store = new TreeMap<>();

    InMemoryTimeDepositRepository(TimeDeposit... timeDeposits) {
        for (TimeDeposit deposit : timeDeposits) {
            store.put(deposit.getId(), copy(deposit));
        }
    }

    @Override
    public List<TimeDeposit> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparingInt(TimeDeposit::getId))
                .map(InMemoryTimeDepositRepository::copy)
                .toList();
    }

    @Override
    public void saveAll(List<TimeDeposit> timeDeposits) {
        timeDeposits.forEach(deposit -> store.put(deposit.getId(), copy(deposit)));
    }

    TimeDeposit get(int id) {
        return copy(store.get(id));
    }

    private static TimeDeposit copy(TimeDeposit deposit) {
        return new TimeDeposit(deposit.getId(), deposit.getPlanType(), deposit.getBalance(), deposit.getDays());
    }
}
