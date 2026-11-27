package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.PostgresTestcontainer;
import org.ikigaidigital.Withdrawal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestcontainer.class)
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class WithdrawalPersistenceAdapterTest {

    @Autowired
    private WithdrawalPersistenceAdapter adapter;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void mapsAllColumns() {
        assertThat(adapter.findAll()).contains(
                new Withdrawal(2, 1, new BigDecimal("250.50"), LocalDate.of(2026, 3, 2)));
    }

    @Test
    void findsAllWithdrawalsOrderedByDateThenId() {
        jdbc.sql("""
                        INSERT INTO withdrawals (id, time_deposit_id, amount, date)
                        VALUES (10, 2, 1.00, DATE '2026-02-20')
                        """)
                .update();

        assertThat(adapter.findAll()).extracting(Withdrawal::id).containsExactly(1, 3, 10, 2, 4);
    }
}
