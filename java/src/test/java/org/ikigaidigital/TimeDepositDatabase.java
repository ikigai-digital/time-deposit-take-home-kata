package org.ikigaidigital;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Shared PostgreSQL Testcontainer. {@code db/schema.sql} is applied once,
 * when the database is first created.
 */
@Testcontainers
public abstract class TimeDepositDatabase {

    @Container
    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("timedeposit")
            .withUsername("timedeposit")
            .withPassword("timedeposit")
            .withInitScript("db/schema.sql");

    protected static Connection connection() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
    }
}
