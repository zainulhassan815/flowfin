-- Technical review: validation queries.
-- Each section announces what it's checking and the expected result.
-- Run with: sqlite3 flowfin.db < review.sql

.headers on
.mode column

------------------------------------------------------------------------------
-- A · Schema introspection
------------------------------------------------------------------------------

.print
.print [A1] foreign_keys pragma — expect: 1
PRAGMA foreign_keys;

.print
.print [A2] integrity_check — expect: ok
PRAGMA integrity_check;

------------------------------------------------------------------------------
-- B · Conservation laws
------------------------------------------------------------------------------

.print
.print [B1] System-wide conservation:
.print      Σ(balances) − Σ(openings) must equal net external flow
.print      (INCOME + BORROW + REPAY_IN − EXPENSE − LEND − REPAY_OUT).
.print      Expect: verdict = OK.

WITH
  totals AS (
    SELECT SUM(opening_balance_minor
                 + COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id), 0)
                 - COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id), 0))
             - SUM(opening_balance_minor) AS system_delta
    FROM accounts a
  ),
  external AS (
    SELECT COALESCE(SUM(CASE
      WHEN kind IN ('INCOME','DEBT_BORROW','DEBT_REPAY_IN')   THEN  amount_minor
      WHEN kind IN ('EXPENSE','DEBT_LEND','DEBT_REPAY_OUT')   THEN -amount_minor
      ELSE 0
    END), 0) AS net_external FROM transactions
  )
SELECT totals.system_delta, external.net_external,
       CASE WHEN totals.system_delta = external.net_external THEN 'OK' ELSE 'FAIL' END AS verdict
FROM totals, external;

------------------------------------------------------------------------------
-- C · Referential integrity & cycle consistency
------------------------------------------------------------------------------

.print
.print [C1] Orphan FKs in transactions — EXPECT 0 rows
SELECT t.id, 'from_account'  AS broken FROM transactions t LEFT JOIN accounts            a ON a.id = t.from_account_id WHERE t.from_account_id IS NOT NULL AND a.id IS NULL
UNION ALL SELECT t.id, 'to_account'    FROM transactions t LEFT JOIN accounts            a ON a.id = t.to_account_id   WHERE t.to_account_id   IS NOT NULL AND a.id IS NULL
UNION ALL SELECT t.id, 'category'      FROM transactions t LEFT JOIN categories          c ON c.id = t.category_id     WHERE t.category_id     IS NOT NULL AND c.id IS NULL
UNION ALL SELECT t.id, 'debt'          FROM transactions t LEFT JOIN debts               d ON d.id = t.debt_id         WHERE t.debt_id         IS NOT NULL AND d.id IS NULL
UNION ALL SELECT t.id, 'recurring'     FROM transactions t LEFT JOIN recurring_schedules r ON r.id = t.recurring_id    WHERE t.recurring_id    IS NOT NULL AND r.id IS NULL;

.print
.print [C2] Debt ↔ origin transaction cycle consistency — EXPECT 0 rows
SELECT d.id AS debt_id, 'origin_tx debt_id mismatch' AS issue
FROM debts d JOIN transactions t ON t.id = d.origin_transaction_id
WHERE t.debt_id IS NULL OR t.debt_id != d.id;

------------------------------------------------------------------------------
-- D · Invariants the schema does NOT enforce
-- (Each of these should ideally be impossible at write time.
--  The query catches violations; the schema column lists what would need
--  to be added.)
------------------------------------------------------------------------------

.print
.print [D1] Self-loops (from_account_id = to_account_id) — EXPECT 0 rows
.print      Gap: schema allows TRANSFER/ALLOCATION/REALLOCATION/etc with
.print      same source and destination.
SELECT id, kind FROM transactions WHERE from_account_id = to_account_id;

.print
.print [D2] Currency mismatch on multi-endpoint transactions — EXPECT 0 rows
.print      Gap: no trigger enforces afrom.currency = ato.currency.
SELECT t.id, t.kind, afrom.currency AS from_ccy, ato.currency AS to_ccy
FROM transactions t
JOIN accounts afrom ON afrom.id = t.from_account_id
JOIN accounts ato   ON ato.id   = t.to_account_id
WHERE afrom.currency != ato.currency;

.print
.print [D3] Category scope mismatch — EXPECT 0 rows
.print      Gap: nothing forces EXPENSE.kind to use a scope=EXPENSE category.
SELECT t.id, t.kind, c.name AS category, c.scope
FROM transactions t JOIN categories c ON c.id = t.category_id
WHERE (t.kind = 'INCOME'  AND c.scope != 'INCOME')
   OR (t.kind = 'EXPENSE' AND c.scope != 'EXPENSE');

.print
.print [D4] REALLOCATION across different real parents — EXPECT 0 rows
.print      Gap: moves allocation across real accounts, changing where
.print      physical money sits. Probably never intended.
SELECT t.id, afrom.name AS from_budget, ato.name AS to_budget
FROM transactions t
JOIN accounts afrom ON afrom.id = t.from_account_id
JOIN accounts ato   ON ato.id   = t.to_account_id
WHERE t.kind = 'REALLOCATION'
  AND afrom.parent_account_id != ato.parent_account_id;

.print
.print [D5] Debt over-payment on still-ACTIVE debts — EXPECT 0 rows
.print      Gap: schema does not bound the sum of repayments.
WITH paid AS (
  SELECT debt_id, SUM(amount_minor) AS total_paid
  FROM transactions WHERE kind IN ('DEBT_REPAY_OUT','DEBT_REPAY_IN')
  GROUP BY debt_id
)
SELECT d.id, d.original_amount_minor, p.total_paid, d.status
FROM debts d JOIN paid p ON p.debt_id = d.id
WHERE p.total_paid > d.original_amount_minor AND d.status = 'ACTIVE';

.print
.print [D6] Debt currency vs origin transaction currency — EXPECT 0 rows
.print      Gap: schema sets debt.currency by convention; no trigger enforces.
SELECT d.id, d.currency AS debt_ccy, COALESCE(afrom.currency, ato.currency) AS tx_ccy
FROM debts d JOIN transactions t ON t.id = d.origin_transaction_id
LEFT JOIN accounts afrom ON afrom.id = t.from_account_id
LEFT JOIN accounts ato   ON ato.id   = t.to_account_id
WHERE d.currency != COALESCE(afrom.currency, ato.currency);

.print
.print [D7] Transactions written against an archived account — EXPECT 0 rows
.print      Gap: only the UI prevents picking an archived account.
SELECT t.id, t.kind, a.name, a.archived_at
FROM transactions t
JOIN accounts a ON a.id IN (t.from_account_id, t.to_account_id)
WHERE a.archived_at IS NOT NULL AND t.recorded_at > a.archived_at;

------------------------------------------------------------------------------
-- E · Time consistency
------------------------------------------------------------------------------

.print
.print [E1] created_at > updated_at anywhere — EXPECT 0 rows
SELECT 'transactions' AS tbl, hex(id) AS id FROM transactions        WHERE created_at > updated_at
UNION ALL SELECT 'accounts',           hex(id) FROM accounts            WHERE created_at > updated_at
UNION ALL SELECT 'categories',         hex(id) FROM categories          WHERE created_at > updated_at
UNION ALL SELECT 'persons',            hex(id) FROM persons             WHERE created_at > updated_at
UNION ALL SELECT 'debts',              hex(id) FROM debts               WHERE created_at > updated_at
UNION ALL SELECT 'recurring',          hex(id) FROM recurring_schedules WHERE created_at > updated_at;

.print
.print [E2] Future-dated transactions (recorded_at > today) — EXPECT 0 rows
SELECT hex(id), kind, recorded_at
FROM transactions
WHERE recorded_at > strftime('%s','2026-05-28') * 1000;

------------------------------------------------------------------------------
-- F · Performance: EXPLAIN QUERY PLAN on hot paths
------------------------------------------------------------------------------

.print
.print [F1] EXPLAIN — recent transactions feed
EXPLAIN QUERY PLAN
SELECT * FROM transactions ORDER BY recorded_at DESC LIMIT 10;

.print
.print [F2] EXPLAIN — account history (Bank)
EXPLAIN QUERY PLAN
SELECT * FROM transactions
WHERE from_account_id = X'A1' OR to_account_id = X'A1'
ORDER BY recorded_at DESC;

.print
.print [F3] EXPLAIN — monthly expense report by category
EXPLAIN QUERY PLAN
SELECT category_id, SUM(amount_minor)
FROM transactions
WHERE kind = 'EXPENSE'
  AND recorded_at >= strftime('%s','2026-05-01')*1000
  AND recorded_at <  strftime('%s','2026-06-01')*1000
GROUP BY category_id;

.print
.print [F4] EXPLAIN — pending recurring
EXPLAIN QUERY PLAN
SELECT * FROM recurring_schedules
WHERE status = 'ACTIVE' AND next_due_at <= strftime('%s','2026-05-28')*1000;

.print
.print [F5] EXPLAIN — debt remaining
EXPLAIN QUERY PLAN
SELECT debt_id, SUM(amount_minor) FROM transactions
WHERE debt_id IS NOT NULL
GROUP BY debt_id;
