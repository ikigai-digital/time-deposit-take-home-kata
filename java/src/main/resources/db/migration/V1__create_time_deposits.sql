-- Preserve the README's mixed-case names with quotes; PostgreSQL otherwise folds them to lowercase.
CREATE TABLE "timeDeposits" (
    -- IDs are supplied by the data-loading scripts; no creation endpoint is required.
    id INTEGER PRIMARY KEY,
    "planType" TEXT NOT NULL,
    days INTEGER NOT NULL,
    -- Unconstrained NUMERIC stores decimal values without imposing extra rounding to cents.
    balance NUMERIC NOT NULL
);

CREATE TABLE withdrawals (
    id INTEGER PRIMARY KEY,
    -- Multiple withdrawal rows may refer to the same deposit.
    "timeDepositId" INTEGER NOT NULL,
    amount NUMERIC NOT NULL,
    -- The requirement specifies a calendar date, not a timestamp.
    date DATE NOT NULL,
    -- A withdrawal must belong to an existing deposit; no cascading deletion is introduced.
    CONSTRAINT fk_withdrawals_time_deposit
        FOREIGN KEY ("timeDepositId") REFERENCES "timeDeposits" (id)
);
