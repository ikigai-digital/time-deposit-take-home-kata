-- src/main/resources/db/migration/V1__Create_time_deposits_and_withdrawals_tables.sql

CREATE TABLE time_deposits (
    id SERIAL PRIMARY KEY,
    plan_type VARCHAR(255) NOT NULL,
    days INT NOT NULL,
    balance DECIMAL(19, 4) NOT NULL
);

CREATE TABLE withdrawals (
    id SERIAL PRIMARY KEY,
    time_deposit_id INT NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    date DATE NOT NULL,
    CONSTRAINT fk_time_deposit
        FOREIGN KEY(time_deposit_id)
        REFERENCES time_deposits(id)
);