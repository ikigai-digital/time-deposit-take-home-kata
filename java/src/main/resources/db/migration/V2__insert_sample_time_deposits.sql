-- Sample rows for the three plan types the interest rules recognize.
-- The student deposit has no withdrawals. Identifiers are fixed so the API
-- test can assert this exact set.

INSERT INTO "timeDeposits" (id, "planType", days, balance) VALUES
    (1, 'basic', 45, 1234567.00),
    (2, 'student', 200, 8000.00),
    (3, 'premium', 60, 25000.50);

INSERT INTO withdrawals (id, "timeDepositId", amount, date) VALUES
    (1, 1, 100.50, DATE '2026-01-15'),
    (2, 1, 25.00, DATE '2026-02-01'),
    (3, 3, 500.00, DATE '2026-03-10');
