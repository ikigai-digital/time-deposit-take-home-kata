-- Sample data so the endpoints can be tried straight away via Swagger UI.
-- Kept outside db/migration (see spring.flyway.locations) so tests and other environments can leave it out.
-- Each row exercises a rule of the interest calculation; the expected balance after one update is noted.

INSERT INTO time_deposits (id, plan_type, days, balance)
VALUES (1, 'basic', 45, 10000.00),   -- 1% p.a.            -> 10008.33
       (2, 'basic', 30, 10000.00),   -- first 30 days       -> 10000.00 (no interest)
       (3, 'student', 200, 5000.00), -- 3% p.a.            -> 5012.50
       (4, 'student', 400, 5000.00), -- older than 1 year   -> 5000.00 (no interest)
       (5, 'premium', 45, 20000.00), -- interest after 45d  -> 20000.00 (no interest yet)
       (6, 'premium', 90, 20000.00), -- 5% p.a.            -> 20083.33
       (7, 'gold', 100, 1000.00);    -- unknown plan type   -> 1000.00 (no interest)

INSERT INTO withdrawals (id, time_deposit_id, amount, date)
VALUES (1, 1, 500.00, '2026-01-15'),
       (2, 1, 250.50, '2026-03-02'),
       (3, 3, 100.00, '2026-02-20'),
       (4, 6, 1000.00, '2026-04-10');
