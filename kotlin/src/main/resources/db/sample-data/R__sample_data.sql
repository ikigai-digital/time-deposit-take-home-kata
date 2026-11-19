INSERT INTO "timeDeposits" ("id", "planType", "days", "balance")
VALUES (1, 'basic', 45, 1234567.00),
       (2, 'basic', 20, 5000.00),
       (3, 'student', 100, 1000.00),
       (4, 'student', 400, 1000.00),
       (5, 'premium', 40, 10000.00),
       (6, 'premium', 90, 10000.00);

INSERT INTO "withdrawals" ("id", "timeDepositId", "amount", "date")
VALUES (1, 1, 1000.00, '2026-01-15'),
       (2, 1, 250.50, '2026-02-01'),
       (3, 6, 500.00, '2026-03-10');

ALTER TABLE "timeDeposits" ALTER COLUMN "id" RESTART WITH 7;
ALTER TABLE "withdrawals" ALTER COLUMN "id" RESTART WITH 4;
