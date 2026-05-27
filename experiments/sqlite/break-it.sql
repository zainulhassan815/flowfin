-- "Break it on purpose" — try to insert rows that should be illegal.
-- Each one with FKs ON. We report whether the schema rejected it.
-- Run with: ./run.sh && sqlite3 -cmd 'PRAGMA foreign_keys=ON' flowfin.db < break-it.sql

.bail off
.print
.print Each test: if you see Error → schema caught it.
.print          if you see (no error) → SCHEMA GAP, the row landed.
.print

.print [D1] self-loop TRANSFER (Bank → Bank, 100):
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, recorded_at, created_at, updated_at)
VALUES (X'BAD1', 'TRANSFER', X'A1', X'A1', 100, 0, 0, 0);
SELECT 'leaked: ' || hex(id) FROM transactions WHERE id = X'BAD1';

.print
.print [D2] cross-currency: insert a USD account, then TRANSFER PKR Bank → USD account:
INSERT INTO accounts (id, name, type, currency, opening_balance_minor, created_at, updated_at)
VALUES (X'A9', 'USD wallet', 'REAL', 'USD', 0, 0, 0);
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, recorded_at, created_at, updated_at)
VALUES (X'BAD2', 'TRANSFER', X'A1', X'A9', 100, 0, 0, 0);
SELECT 'leaked: ' || hex(id) FROM transactions WHERE id = X'BAD2';

.print
.print [D3] category-scope mismatch: EXPENSE row pointing at INCOME category (Salary):
INSERT INTO transactions (id, kind, from_account_id, amount_minor, category_id, recorded_at, created_at, updated_at)
VALUES (X'BAD3', 'EXPENSE', X'A1', 100, X'C1', 0, 0, 0);
SELECT 'leaked: ' || hex(id) FROM transactions WHERE id = X'BAD3';

.print
.print [D5] debt over-payment: repay Ahmed another 100,000 (he was owed 8,000 total):
INSERT INTO transactions (id, kind, from_account_id, amount_minor, debt_id, recorded_at, created_at, updated_at)
VALUES (X'BAD5', 'DEBT_REPAY_OUT', X'A1', 10000000, X'E1', 0, 0, 0);
SELECT 'leaked: ' || hex(id), 'paid=' || (SELECT SUM(amount_minor) FROM transactions WHERE debt_id = X'E1' AND kind = 'DEBT_REPAY_OUT')
FROM transactions WHERE id = X'BAD5';

.print
.print [D7] transaction against archived account: archive JazzCash, then spend from it:
UPDATE accounts SET archived_at = strftime('%s','2026-05-01')*1000 WHERE id = X'A3';
INSERT INTO transactions (id, kind, from_account_id, amount_minor, category_id, recorded_at, created_at, updated_at)
VALUES (X'BAD7', 'EXPENSE', X'A3', 100, X'C2', strftime('%s','2026-05-20')*1000, 0, 0);
SELECT 'leaked: ' || hex(id) FROM transactions WHERE id = X'BAD7';

.print
.print [D4] REALLOCATION across different real parents:
.print      Create a budget parented to Cash, then try Food (parent Bank) → it.
INSERT INTO accounts (id, name, type, currency, parent_account_id, opening_balance_minor, created_at, updated_at)
VALUES (X'B9', 'CashWallet', 'BUDGET', 'PKR', X'A2', 0, 0, 0);
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, recorded_at, created_at, updated_at)
VALUES (X'BAD4', 'REALLOCATION', X'B1', X'B9', 100, 0, 0, 0);
SELECT 'leaked: ' || hex(id) FROM transactions WHERE id = X'BAD4';

.print
.print [D6] debt currency != origin transaction account currency:
.print      Insert a PKR borrow transaction, then a USD debt pointing at it.
BEGIN;
INSERT INTO transactions (id, kind, to_account_id, amount_minor, recorded_at, debt_id, created_at, updated_at)
VALUES (X'D60D', 'DEBT_BORROW', X'A1', 5000, 0, X'E9', 0, 0);
INSERT INTO debts (id, person_id, direction, original_amount_minor, currency, status, origin_transaction_id, created_at, updated_at)
VALUES (X'E9', X'D1', 'I_OWE', 5000, 'USD', 'ACTIVE', X'D60D', 0, 0);
COMMIT;
SELECT 'leaked: ' || hex(id) FROM debts WHERE id = X'E9';
