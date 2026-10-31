package org.ikigaidigital.adapter.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.TimeDepositStore;
import org.ikigaidigital.application.TimeDepositView;
import org.ikigaidigital.application.WithdrawalView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Implements the application's storage contract using the configured PostgreSQL datasource. */
@Repository
public class JdbcTimeDepositStore implements TimeDepositStore {
    // Reuse the same ordered deposit query for response views and calculation inputs.
    private static final String SELECT_DEPOSITS = """
            SELECT id, "planType", balance, days
            FROM "timeDeposits"
            ORDER BY id
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcTimeDepositStore(JdbcTemplate jdbcTemplate) {
        // Spring supplies the template; it participates in the service's transaction.
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TimeDepositView> findAll() {
        // Read withdrawal history once, rather than running a query for every deposit.
        Map<Integer, List<WithdrawalView>> withdrawalsByDeposit = jdbcTemplate.query("""
                SELECT id, "timeDepositId", amount, date
                FROM withdrawals
                ORDER BY id
                """, resultSet -> {
            Map<Integer, List<WithdrawalView>> groupedWithdrawals = new HashMap<>();
            while (resultSet.next()) {
                WithdrawalView withdrawal = new WithdrawalView(
                        resultSet.getInt("id"), resultSet.getBigDecimal("amount"),
                        resultSet.getObject("date", LocalDate.class));
                groupedWithdrawals.computeIfAbsent(resultSet.getInt("timeDepositId"),
                        depositId -> new ArrayList<>()).add(withdrawal);
            }
            return groupedWithdrawals;
        });

        // Driving this query from deposits retains deposits that have no withdrawals.
        return jdbcTemplate.query(SELECT_DEPOSITS, (resultSet, rowNumber) -> new TimeDepositView(
                resultSet.getInt("id"), resultSet.getString("planType"),
                resultSet.getBigDecimal("balance"), resultSet.getInt("days"),
                withdrawalsByDeposit.getOrDefault(resultSet.getInt("id"), List.of())));
    }

    @Override
    public List<TimeDeposit> findAllForCalculation() {
        // Lock rows in ID order until the service transaction commits or rolls back.
        // A concurrent update then reads the saved balance instead of losing an interest credit.
        // Convert decimals to Double only for the existing shared calculation model.
        return jdbcTemplate.query(SELECT_DEPOSITS + " FOR UPDATE", (resultSet, rowNumber) -> new TimeDeposit(
                resultSet.getInt("id"), resultSet.getString("planType"),
                resultSet.getBigDecimal("balance").doubleValue(), resultSet.getInt("days")));
    }

    @Override
    public void updateBalances(List<TimeDeposit> deposits) {
        // An empty database needs no batch and must not supply a zero batch size.
        if (deposits.isEmpty()) {
            return;
        }

        // Bind values separately from SQL; update only balances, preserving withdrawal history.
        jdbcTemplate.batchUpdate("""
                UPDATE "timeDeposits" SET balance = ? WHERE id = ?
                """, deposits, deposits.size(), (statement, deposit) -> {
            statement.setBigDecimal(1, BigDecimal.valueOf(deposit.getBalance()));
            statement.setInt(2, deposit.getId());
        });
    }
}
