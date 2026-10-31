-- Optional manual examples: load after the app has applied its Flyway migration.
-- This file is outside application resources, so startup never loads it automatically.
-- Fixed demo IDs are inserted only when absent; reruns do not reset credited balances or history.
BEGIN;

INSERT INTO "timeDeposits" (id, "planType", balance, days)
VALUES
    (1, 'basic',   1200.00,  31), -- Eligible: one update adds 1.00.
    (2, 'student', 1200.00, 365), -- Last eligible student day: one update adds 3.00.
    (3, 'premium', 1200.00,  46), -- First eligible premium day: one update adds 5.00.
    (4, 'basic',   1200.00,  30), -- Waiting period: balance stays unchanged.
    (5, 'student', 1200.00, 366), -- Student eligibility ended: balance stays unchanged.
    (6, 'premium', 1200.00,  45)  -- Premium waiting period: balance stays unchanged.
ON CONFLICT (id) DO NOTHING;

-- Balances above are already current amounts; stored withdrawal history causes no new deduction.
-- Deposit 1 has multiple withdrawals; deposits 3-6 demonstrate empty withdrawal lists.
INSERT INTO withdrawals (id, "timeDepositId", amount, date)
VALUES
    (1, 1,  50.00, DATE '2026-10-01'),
    (2, 1,  25.50, DATE '2026-10-15'),
    (3, 2, 100.00, DATE '2026-10-20')
ON CONFLICT (id) DO NOTHING;

COMMIT;
