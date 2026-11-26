package org.ikigaidigital;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeDepositDatabaseTest extends TimeDepositDatabase {

    @Test
    void timeDepositsTableMatchesTheAssignment() throws SQLException {
        assertThat(columns("timeDeposits")).containsExactly(
                column("id", "integer", false),
                column("planType", "text", false),
                column("days", "integer", false),
                column("balance", "numeric", false)
        );
        assertThat(primaryKeyColumns("timeDeposits")).containsExactly("id");
    }

    @Test
    void withdrawalsTableMatchesTheAssignment() throws SQLException {
        assertThat(columns("withdrawals")).containsExactly(
                column("id", "integer", false),
                column("timeDepositId", "integer", false),
                column("amount", "numeric", false),
                column("date", "date", false)
        );
        assertThat(primaryKeyColumns("withdrawals")).containsExactly("id");
        assertThat(foreignKey("withdrawals", "timeDepositId"))
                .isEqualTo(new ForeignKey("timeDeposits", "id"));
    }

    @Test
    void storesADepositAndItsWithdrawal() throws SQLException {
        try (Connection connection = connection()) {
            connection.setAutoCommit(false);
            insertDeposit(connection, 1, "basic", 45, new BigDecimal("1234567.00"));
            insertWithdrawal(connection, 10, 1, new BigDecimal("100.50"), Date.valueOf("2026-01-15"));

            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT d."planType", d.days, d.balance, w.amount, w.date
                    FROM "timeDeposits" d
                    JOIN withdrawals w ON w."timeDepositId" = d.id
                    WHERE d.id = ?
                    """)) {
                statement.setInt(1, 1);
                try (ResultSet rows = statement.executeQuery()) {
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getString("planType")).isEqualTo("basic");
                    assertThat(rows.getInt("days")).isEqualTo(45);
                    assertThat(rows.getBigDecimal("balance")).isEqualByComparingTo("1234567.00");
                    assertThat(rows.getBigDecimal("amount")).isEqualByComparingTo("100.50");
                    assertThat(rows.getDate("date")).isEqualTo(Date.valueOf("2026-01-15"));
                    assertThat(rows.next()).isFalse();
                }
            }
            connection.rollback();
        }
    }

    @Test
    void rejectsAWithdrawalForAnUnknownDeposit() throws SQLException {
        try (Connection connection = connection()) {
            connection.setAutoCommit(false);
            assertThatThrownBy(() ->
                    insertWithdrawal(connection, 10, 999, new BigDecimal("10.00"), Date.valueOf("2026-01-15")))
                    .isInstanceOf(SQLException.class)
                    .extracting(exception -> ((SQLException) exception).getSQLState())
                    .isEqualTo("23503");
            connection.rollback();
        }
    }

    private static void insertDeposit(Connection connection, int id, String planType, int days, BigDecimal balance)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO "timeDeposits" (id, "planType", days, balance)
                VALUES (?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setString(2, planType);
            statement.setInt(3, days);
            statement.setBigDecimal(4, balance);
            statement.executeUpdate();
        }
    }

    private static void insertWithdrawal(Connection connection, int id, int timeDepositId, BigDecimal amount, Date date)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO withdrawals (id, "timeDepositId", amount, date)
                VALUES (?, ?, ?, ?)
                """)) {
            statement.setInt(1, id);
            statement.setInt(2, timeDepositId);
            statement.setBigDecimal(3, amount);
            statement.setDate(4, date);
            statement.executeUpdate();
        }
    }

    private static List<Column> columns(String table) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT column_name, data_type, is_nullable
                     FROM information_schema.columns
                     WHERE table_schema = 'public' AND table_name = ?
                     ORDER BY ordinal_position
                     """)) {
            statement.setString(1, table);
            try (ResultSet rows = statement.executeQuery()) {
                List<Column> columns = new ArrayList<>();
                while (rows.next()) {
                    columns.add(column(
                            rows.getString("column_name"),
                            rows.getString("data_type"),
                            "YES".equals(rows.getString("is_nullable"))));
                }
                return columns;
            }
        }
    }

    private static List<String> primaryKeyColumns(String table) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT kcu.column_name
                     FROM information_schema.table_constraints tc
                     JOIN information_schema.key_column_usage kcu
                       ON kcu.constraint_name = tc.constraint_name
                      AND kcu.table_schema = tc.table_schema
                     WHERE tc.table_schema = 'public'
                       AND tc.table_name = ?
                       AND tc.constraint_type = 'PRIMARY KEY'
                     ORDER BY kcu.ordinal_position
                     """)) {
            statement.setString(1, table);
            try (ResultSet rows = statement.executeQuery()) {
                List<String> columns = new ArrayList<>();
                while (rows.next()) {
                    columns.add(rows.getString("column_name"));
                }
                return columns;
            }
        }
    }

    private static ForeignKey foreignKey(String table, String column) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT ccu.table_name, ccu.column_name
                     FROM information_schema.table_constraints tc
                     JOIN information_schema.key_column_usage kcu
                       ON kcu.constraint_name = tc.constraint_name
                      AND kcu.table_schema = tc.table_schema
                     JOIN information_schema.constraint_column_usage ccu
                       ON ccu.constraint_name = tc.constraint_name
                      AND ccu.table_schema = tc.table_schema
                     WHERE tc.table_schema = 'public'
                       AND tc.table_name = ?
                       AND kcu.column_name = ?
                       AND tc.constraint_type = 'FOREIGN KEY'
                     """)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet rows = statement.executeQuery()) {
                assertThat(rows.next()).isTrue();
                ForeignKey foreignKey = new ForeignKey(rows.getString("table_name"), rows.getString("column_name"));
                assertThat(rows.next()).isFalse();
                return foreignKey;
            }
        }
    }

    private static Column column(String name, String dataType, boolean nullable) {
        return new Column(name, dataType, nullable);
    }

    private record Column(String name, String dataType, boolean nullable) {
    }

    private record ForeignKey(String table, String column) {
    }
}
