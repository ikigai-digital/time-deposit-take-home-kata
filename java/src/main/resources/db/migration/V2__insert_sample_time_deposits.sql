INSERT INTO time_deposits (plan_type, days, balance)
VALUES ('basic', 45, 10000.00),
       ('basic', 20, 5000.00),
       ('student', 200, 20000.00),
       ('student', 400, 15000.00),
       ('premium', 40, 30000.00),
       ('premium', 120, 1234567.00);

INSERT INTO withdrawals (time_deposit_id, amount, date)
VALUES (1, 500.00, '2026-10-01'),
       (1, 250.00, '2026-10-15'),
       (3, 1000.00, '2026-09-20'),
       (6, 20000.00, '2026-08-05');
