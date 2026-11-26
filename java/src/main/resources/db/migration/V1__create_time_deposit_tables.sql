-- PostgreSQL folds unquoted identifiers to lowercase. CamelCase names from the
-- assignment (timeDeposits, planType, timeDepositId) are quoted so they stay
-- exact. withdrawals, amount, and date are stored as written.
--
-- Decimal maps to NUMERIC (arbitrary precision). String maps to TEXT.
-- Date maps to DATE. Primary keys are plain INTEGER values supplied by the
-- application; the assignment does not ask for generated ids.
-- withdrawals.timeDepositId references timeDeposits.id. Delete behavior is
-- left at PostgreSQL's default (NO ACTION) because none was specified.

CREATE TABLE "timeDeposits" (
    id INTEGER PRIMARY KEY,
    "planType" TEXT NOT NULL,
    days INTEGER NOT NULL,
    balance NUMERIC NOT NULL
);

CREATE TABLE withdrawals (
    id INTEGER PRIMARY KEY,
    "timeDepositId" INTEGER NOT NULL REFERENCES "timeDeposits" (id),
    amount NUMERIC NOT NULL,
    date DATE NOT NULL
);
