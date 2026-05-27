-- Compare two strategies for computing all-account balances.

PRAGMA foreign_keys = ON;
.timer on

.print
.print --- F4a (current): per-account correlated subqueries
SELECT
  a.name,
  a.opening_balance_minor
    + COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id), 0)
    - COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id), 0)
    AS balance_minor
FROM accounts a
WHERE a.archived_at IS NULL;

.print
.print --- F4b: two GROUP BY scans joined to accounts
WITH
  inflows AS (
    SELECT to_account_id AS id, SUM(amount_minor) AS in_minor
    FROM transactions WHERE to_account_id IS NOT NULL
    GROUP BY to_account_id
  ),
  outflows AS (
    SELECT from_account_id AS id, SUM(amount_minor) AS out_minor
    FROM transactions WHERE from_account_id IS NOT NULL
    GROUP BY from_account_id
  )
SELECT
  a.name,
  a.opening_balance_minor
    + COALESCE(i.in_minor, 0)
    - COALESCE(o.out_minor, 0)
    AS balance_minor
FROM accounts a
LEFT JOIN inflows  i ON i.id = a.id
LEFT JOIN outflows o ON o.id = a.id
WHERE a.archived_at IS NULL;

.print
.print --- F4c: single scan with conditional sums
SELECT
  a.name,
  a.opening_balance_minor
    + COALESCE(SUM(CASE WHEN t.to_account_id   = a.id THEN t.amount_minor ELSE 0 END), 0)
    - COALESCE(SUM(CASE WHEN t.from_account_id = a.id THEN t.amount_minor ELSE 0 END), 0)
    AS balance_minor
FROM accounts a
LEFT JOIN transactions t ON t.from_account_id = a.id OR t.to_account_id = a.id
WHERE a.archived_at IS NULL
GROUP BY a.id;

.print
.print --- EXPLAIN for F4a:
EXPLAIN QUERY PLAN
SELECT a.id,
  (SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id) AS in_minor,
  (SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id) AS out_minor
FROM accounts a;

.print
.print --- EXPLAIN for F4b:
EXPLAIN QUERY PLAN
WITH
  inflows AS (
    SELECT to_account_id AS id, SUM(amount_minor) AS in_minor
    FROM transactions WHERE to_account_id IS NOT NULL
    GROUP BY to_account_id
  ),
  outflows AS (
    SELECT from_account_id AS id, SUM(amount_minor) AS out_minor
    FROM transactions WHERE from_account_id IS NOT NULL
    GROUP BY from_account_id
  )
SELECT a.name, COALESCE(i.in_minor,0), COALESCE(o.out_minor,0)
FROM accounts a
LEFT JOIN inflows  i ON i.id = a.id
LEFT JOIN outflows o ON o.id = a.id;
