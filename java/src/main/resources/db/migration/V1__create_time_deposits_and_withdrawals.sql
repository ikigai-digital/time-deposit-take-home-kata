-- Naming: the README specifies `timeDeposits`, `planType` and `timeDepositId`. PostgreSQL folds unquoted
-- identifiers to lower case (timeDeposits -> timedeposits), so snake_case is used instead of quoted
-- camelCase identifiers. Mapping: timeDeposits -> time_deposits, planType -> plan_type,
-- timeDepositId -> time_deposit_id.
--
-- Money: the README asks for Decimal. Amounts are stored in whole cents, NUMERIC(19, 2). The domain keeps
-- using double (TimeDeposit cannot change); the persistence adapter rounds HALF_UP to 2 decimals on write.

CREATE TABLE time_deposits
(
    id        INTEGER        PRIMARY KEY,
    -- No CHECK constraint on known plans: the legacy calculator accepts unknown plan types (no interest).
    plan_type VARCHAR(50)    NOT NULL,
    days      INTEGER        NOT NULL,
    balance   NUMERIC(19, 2) NOT NULL
);

CREATE TABLE withdrawals
(
    id              INTEGER        PRIMARY KEY,
    time_deposit_id INTEGER        NOT NULL REFERENCES time_deposits (id),
    amount          NUMERIC(19, 2) NOT NULL,
    date            DATE           NOT NULL
);

CREATE INDEX idx_withdrawals_time_deposit_id ON withdrawals (time_deposit_id);
