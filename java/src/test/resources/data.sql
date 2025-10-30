INSERT INTO TIMEDEPOSITS (id, planType, days, balance) VALUES (1, 'PREMIUM', 365, 1250);
INSERT INTO TIMEDEPOSITS (id, planType, days, balance) VALUES (2, 'STUDENT', 730, 5000);

INSERT INTO WITHDRAWALS (id, timeDepositId, amount, date) VALUES (1, 1, 10, '2025-09-01 12:00:00 UTC');
INSERT INTO WITHDRAWALS (id, timeDepositId, amount, date) VALUES (2, 1, 20, '2025-10-01 12:00:00 UTC');
INSERT INTO WITHDRAWALS (id, timeDepositId, amount, date) VALUES (3, 2, 20, '2024-06-25 12:00:00 UTC');