package org.ikigaidigital;

import org.flywaydb.core.Flyway;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Shared PostgreSQL Testcontainer. Flyway applies {@code db/migration}
 * once for each database the container creates.
 */
@Testcontainers
public abstract class TimeDepositDatabase {

    @Container
    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("timedeposit")
            .withUsername("timedeposit")
            .withPassword("timedeposit");

    private static String migratedJdbcUrl;

    protected static void migrate() {
        POSTGRES.start();
        String jdbcUrl = POSTGRES.getJdbcUrl();
        if (jdbcUrl.equals(migratedJdbcUrl)) {
            return;
        }
        Flyway.configure()
                .dataSource(jdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
        migratedJdbcUrl = jdbcUrl;
    }

    protected static Connection connection() throws SQLException {
        migrate();
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
    }
}
