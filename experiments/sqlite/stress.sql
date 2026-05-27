-- Stress test: insert 100k transactions, then time the hot-path queries.
-- ~90% EXPENSE, ~5% INCOME, ~5% TRANSFER, spread across 5 years.

PRAGMA journal_mode = WAL;
PRAGMA synchronous  = NORMAL;
PRAGMA foreign_keys = ON;

.print
.print === BULK INSERT 100,000 transactions ===
.timer on

BEGIN;

-- 90,000 EXPENSE rows: random REAL/BUDGET account, random EXPENSE category.
WITH RECURSIVE
  series(i) AS (SELECT 1 UNION ALL SELECT i+1 FROM series WHERE i < 90000)
INSERT INTO transactions (id, kind, from_account_id, amount_minor, category_id, recorded_at, created_at, updated_at)
SELECT
  randomblob(16),
  'EXPENSE',
  (SELECT id FROM accounts WHERE archived_at IS NULL ORDER BY random() LIMIT 1),
  abs(random() % 50000) + 1,
  (SELECT id FROM categories WHERE scope = 'EXPENSE' AND archived_at IS NULL ORDER BY random() LIMIT 1),
  strftime('%s','2021-01-01')*1000 + (abs(random()) % (5*365*86400000)),
  strftime('%s','now')*1000,
  strftime('%s','now')*1000
FROM series;

-- 5,000 INCOME rows to Bank.
WITH RECURSIVE
  series(i) AS (SELECT 1 UNION ALL SELECT i+1 FROM series WHERE i < 5000)
INSERT INTO transactions (id, kind, to_account_id, amount_minor, category_id, recorded_at, created_at, updated_at)
SELECT
  randomblob(16),
  'INCOME',
  X'A1',
  abs(random() % 1000000) + 100,
  X'C1',
  strftime('%s','2021-01-01')*1000 + (abs(random()) % (5*365*86400000)),
  strftime('%s','now')*1000,
  strftime('%s','now')*1000
FROM series;

-- 5,000 TRANSFER Bank → Cash.
WITH RECURSIVE
  series(i) AS (SELECT 1 UNION ALL SELECT i+1 FROM series WHERE i < 5000)
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, recorded_at, created_at, updated_at)
SELECT
  randomblob(16),
  'TRANSFER',
  X'A1', X'A2',
  abs(random() % 100000) + 1,
  strftime('%s','2021-01-01')*1000 + (abs(random()) % (5*365*86400000)),
  strftime('%s','now')*1000,
  strftime('%s','now')*1000
FROM series;

COMMIT;

ANALYZE;

.print
.print === ROW COUNTS ===
SELECT COUNT(*) AS transactions FROM transactions;
SELECT kind, COUNT(*) AS n FROM transactions GROUP BY kind ORDER BY n DESC;

.print
.print === DB FILE SIZE ===
SELECT page_count * page_size AS bytes,
       printf('%.2f MB', (page_count * page_size) / (1024.0 * 1024.0)) AS size
FROM pragma_page_count(), pragma_page_size();

.print
.print === HOT QUERY TIMINGS ===
.print
.print [F1] recent transactions feed (the home screen)
SELECT t.id, t.kind, t.amount_minor, t.recorded_at
FROM transactions t
ORDER BY t.recorded_at DESC
LIMIT 10;

.print
.print [F2] account history — Bank
SELECT t.id, t.kind, t.amount_minor, t.recorded_at
FROM transactions t
WHERE t.from_account_id = X'A1' OR t.to_account_id = X'A1'
ORDER BY t.recorded_at DESC
LIMIT 50;

.print
.print [F3] monthly expense report by category (Jan 2024)
SELECT category_id, SUM(amount_minor) AS total, COUNT(*) AS n
FROM transactions
WHERE kind = 'EXPENSE'
  AND recorded_at >= strftime('%s','2024-01-01')*1000
  AND recorded_at <  strftime('%s','2024-02-01')*1000
GROUP BY category_id
ORDER BY total DESC;

.print
.print [F4] computed balance for every account
SELECT
  a.name,
  a.opening_balance_minor
    + COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id), 0)
    - COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id), 0)
    AS balance_minor
FROM accounts a
WHERE a.archived_at IS NULL;

.print
.print [F5] full-system conservation check (the B1 invariant)
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
