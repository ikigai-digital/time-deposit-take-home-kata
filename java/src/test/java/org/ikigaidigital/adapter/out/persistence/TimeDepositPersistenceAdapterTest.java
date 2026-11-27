package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import org.ikigaidigital.PostgresTestcontainer;
import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the sample data (db/sample-data). Each test rolls back, leaving the data intact for other tests.
 */
@SpringBootTest
@Import(PostgresTestcontainer.class)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class TimeDepositPersistenceAdapterTest {

    @Autowired
    private TimeDepositPersistenceAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void findsAllTimeDepositsOrderedById() {
        List<TimeDeposit> deposits = adapter.findAll();

        assertThat(deposits).extracting(TimeDeposit::getId).containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(deposits.get(0))
                .returns("basic", TimeDeposit::getPlanType)
                .returns(10_000.00, TimeDeposit::getBalance)
                .returns(45, TimeDeposit::getDays);
    }

    @Test
    void savesUpdatedBalances() {
        List<TimeDeposit> deposits = adapter.findAll();
        deposits.get(0).setBalance(10_008.33);
        deposits.get(2).setBalance(5_012.50);

        adapter.saveAll(deposits);
        flushAndClear();

        assertThat(balance(1)).isEqualByComparingTo("10008.33");
        assertThat(balance(2)).isEqualByComparingTo("10000.00");
        assertThat(balance(3)).isEqualByComparingTo("5012.50");
    }

    @Test
    void roundsBalanceToCentsHalfUpOnSave() {
        adapter.saveAll(List.of(new TimeDeposit(1, "basic", 10_000.005, 45)));
        flushAndClear();

        assertThat(balance(1)).isEqualByComparingTo("10000.01");
    }

    @Test
    void writesOnlyTheBalance() {
        adapter.saveAll(List.of(new TimeDeposit(1, "premium", 1.00, 999)));
        flushAndClear();

        assertThat(jdbc.sql("SELECT plan_type, days FROM time_deposits WHERE id = 1").query().singleRow())
                .isEqualTo(Map.of("plan_type", "basic", "days", 45));
        assertThat(balance(1)).isEqualByComparingTo("1.00");
    }

    @Test
    void ignoresUnknownTimeDeposits() {
        adapter.saveAll(List.of(new TimeDeposit(12_345, "basic", 1.00, 45)));
        flushAndClear();

        assertThat(jdbc.sql("SELECT count(*) FROM time_deposits").query(Integer.class).single()).isEqualTo(7);
    }

    @Test
    void savedBalancesAreReadBackUnchanged() {
        adapter.saveAll(List.of(new TimeDeposit(6, "premium", 20_083.33, 90)));
        flushAndClear();

        assertThat(adapter.findAll().get(5).getBalance()).isEqualTo(20_083.33);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private BigDecimal balance(int id) {
        return jdbc.sql("SELECT balance FROM time_deposits WHERE id = :id")
                .param("id", id)
                .query(BigDecimal.class)
                .single();
    }
}
