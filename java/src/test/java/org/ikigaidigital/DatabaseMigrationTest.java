package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the Flyway migrations against a real PostgreSQL: schema shape, constraints and sample data.
 */
@SpringBootTest
@Import(PostgresTestcontainer.class)
@Testcontainers(disabledWithoutDocker = true)
class DatabaseMigrationTest {

    @Autowired
    private JdbcClient jdbc;

    @Test
    void createsTimeDepositsTable() {
        assertThat(columns("time_deposits")).containsExactly(
                Map.of("column_name", "id", "data_type", "integer", "is_nullable", "NO"),
                Map.of("column_name", "plan_type", "data_type", "character varying", "is_nullable", "NO"),
                Map.of("column_name", "days", "data_type", "integer", "is_nullable", "NO"),
                Map.of("column_name", "balance", "data_type", "numeric", "is_nullable", "NO"));
    }

    @Test
    void createsWithdrawalsTable() {
        assertThat(columns("withdrawals")).containsExactly(
                Map.of("column_name", "id", "data_type", "integer", "is_nullable", "NO"),
                Map.of("column_name", "time_deposit_id", "data_type", "integer", "is_nullable", "NO"),
                Map.of("column_name", "amount", "data_type", "numeric", "is_nullable", "NO"),
                Map.of("column_name", "date", "data_type", "date", "is_nullable", "NO"));
    }

    @Test
    void storesMoneyWithTwoDecimalPlaces() {
        List<Integer> scales = jdbc.sql("""
                        SELECT numeric_scale FROM information_schema.columns
                        WHERE (table_name, column_name) IN (('time_deposits', 'balance'), ('withdrawals', 'amount'))
                        """)
                .query(Integer.class)
                .list();

        assertThat(scales).containsOnly(2).hasSize(2);
    }

    @Test
    void withdrawalMustReferenceExistingTimeDeposit() {
        assertThatThrownBy(() -> jdbc.sql("""
                        INSERT INTO withdrawals (id, time_deposit_id, amount, date)
                        VALUES (999, 12345, 1.00, DATE '2026-01-01')
                        """)
                .update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void loadsSampleData() {
        assertThat(jdbc.sql("SELECT plan_type FROM time_deposits ORDER BY id").query(String.class).list())
                .containsExactly("basic", "basic", "student", "student", "premium", "premium", "gold");
        assertThat(jdbc.sql("SELECT balance FROM time_deposits WHERE id = 1").query(BigDecimal.class).single())
                .isEqualByComparingTo("10000.00");
        assertThat(jdbc.sql("SELECT count(*) FROM withdrawals").query(Integer.class).single())
                .isEqualTo(4);
    }

    private List<Map<String, Object>> columns(String table) {
        return jdbc.sql("""
                        SELECT column_name, data_type, is_nullable FROM information_schema.columns
                        WHERE table_name = :table ORDER BY ordinal_position
                        """)
                .param("table", table)
                .query()
                .listOfRows();
    }
}
