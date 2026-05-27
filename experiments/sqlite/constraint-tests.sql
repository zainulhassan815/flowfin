-- Constraint smoke tests. Every statement here should FAIL.
-- Run with: sqlite3 flowfin.db < constraint-tests.sql 2>&1 | grep -E "Error|OK"
-- We expect: a string of "Error" lines, one per test.

.bail off

.print [test 1] ghost transaction (all account fks null) should fail
INSERT INTO transactions (id, kind, amount_minor, recorded_at, created_at, updated_at)
VALUES (X'F0', 'EXPENSE', 100, 0, 0, 0);

.print [test 2] EXPENSE with to_account_id set should fail (shape mismatch)
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, recorded_at, created_at, updated_at)
VALUES (X'F1', 'EXPENSE', X'A1', X'A2', 100, X'C2', 0, 0, 0);

.print [test 3] negative amount should fail
INSERT INTO transactions (id, kind, from_account_id, amount_minor, category_id, recorded_at, created_at, updated_at)
VALUES (X'F2', 'EXPENSE', X'A1', -5, X'C2', 0, 0, 0);

.print [test 4] recurring + debt on same row should fail
INSERT INTO transactions (id, kind, from_account_id, amount_minor, recorded_at, recurring_id, debt_id, created_at, updated_at)
VALUES (X'F3', 'DEBT_REPAY_OUT', X'A1', 100, 0, X'F1', X'E1', 0, 0);

.print [test 5] BUDGET account with no parent should fail (CHECK)
INSERT INTO accounts (id, name, type, currency, parent_account_id, opening_balance_minor, created_at, updated_at)
VALUES (X'BB', 'Floating budget', 'BUDGET', 'PKR', NULL, 0, 0, 0);

.print [test 6] BUDGET pointing at another BUDGET should fail (trigger)
INSERT INTO accounts (id, name, type, currency, parent_account_id, opening_balance_minor, created_at, updated_at)
VALUES (X'BC', 'Sub-budget', 'BUDGET', 'PKR', X'B1', 0, 0, 0);

.print [test 7] duplicate account name (active) should fail (partial unique idx)
INSERT INTO accounts (id, name, type, currency, opening_balance_minor, created_at, updated_at)
VALUES (X'BD', 'Bank', 'REAL', 'PKR', 0, 0, 0);

.print [test 8] case-variant person name should fail (case-insensitive unique)
INSERT INTO persons (id, name, avatar_tint_index, created_at, updated_at)
VALUES (X'DD', 'AHMED', 2, 0, 0);

.print [test 9] BUDGET currency must match parent's currency
INSERT INTO accounts (id, name, type, currency, parent_account_id, opening_balance_minor, created_at, updated_at)
VALUES (X'BE', 'USD budget', 'BUDGET', 'USD', X'A1', 0, 0, 0);

.print [test 10] recurring with both day_of_week AND day_of_month should fail
INSERT INTO recurring_schedules (id, name, amount_minor, from_account_id, category_id, cadence, day_of_week, day_of_month, next_due_at, created_at, updated_at)
VALUES (X'FF', 'Bad recurring', 100, X'A1', X'C6', 'MONTHLY', 3, 15, 0, 0, 0);
