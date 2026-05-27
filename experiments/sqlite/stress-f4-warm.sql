-- Warm the cache, then run each variant 3 times.
PRAGMA foreign_keys = ON;
.timer on

-- Warm pass: scan transactions to fill OS file cache.
SELECT COUNT(*) FROM transactions;

.print
.print --- F4a x3 (correlated subqueries)
SELECT a.name,
  a.opening_balance_minor
    + COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id), 0)
    - COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id), 0) AS b
FROM accounts a WHERE a.archived_at IS NULL;
SELECT a.name,
  a.opening_balance_minor
    + COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id), 0)
    - COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id), 0) AS b
FROM accounts a WHERE a.archived_at IS NULL;
SELECT a.name,
  a.opening_balance_minor
    + COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id), 0)
    - COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id), 0) AS b
FROM accounts a WHERE a.archived_at IS NULL;

.print
.print --- F4b x3 (two GROUP BY scans + join)
WITH inflows  AS (SELECT to_account_id   AS id, SUM(amount_minor) AS s FROM transactions WHERE to_account_id   IS NOT NULL GROUP BY to_account_id),
     outflows AS (SELECT from_account_id AS id, SUM(amount_minor) AS s FROM transactions WHERE from_account_id IS NOT NULL GROUP BY from_account_id)
SELECT a.name, a.opening_balance_minor + COALESCE(i.s,0) - COALESCE(o.s,0)
FROM accounts a LEFT JOIN inflows i ON i.id=a.id LEFT JOIN outflows o ON o.id=a.id WHERE a.archived_at IS NULL;
WITH inflows  AS (SELECT to_account_id   AS id, SUM(amount_minor) AS s FROM transactions WHERE to_account_id   IS NOT NULL GROUP BY to_account_id),
     outflows AS (SELECT from_account_id AS id, SUM(amount_minor) AS s FROM transactions WHERE from_account_id IS NOT NULL GROUP BY from_account_id)
SELECT a.name, a.opening_balance_minor + COALESCE(i.s,0) - COALESCE(o.s,0)
FROM accounts a LEFT JOIN inflows i ON i.id=a.id LEFT JOIN outflows o ON o.id=a.id WHERE a.archived_at IS NULL;
WITH inflows  AS (SELECT to_account_id   AS id, SUM(amount_minor) AS s FROM transactions WHERE to_account_id   IS NOT NULL GROUP BY to_account_id),
     outflows AS (SELECT from_account_id AS id, SUM(amount_minor) AS s FROM transactions WHERE from_account_id IS NOT NULL GROUP BY from_account_id)
SELECT a.name, a.opening_balance_minor + COALESCE(i.s,0) - COALESCE(o.s,0)
FROM accounts a LEFT JOIN inflows i ON i.id=a.id LEFT JOIN outflows o ON o.id=a.id WHERE a.archived_at IS NULL;

.print
.print --- F4c x3 (single scan, conditional sum)
SELECT a.name,
  a.opening_balance_minor
    + COALESCE(SUM(CASE WHEN t.to_account_id   = a.id THEN t.amount_minor ELSE 0 END), 0)
    - COALESCE(SUM(CASE WHEN t.from_account_id = a.id THEN t.amount_minor ELSE 0 END), 0)
FROM accounts a
LEFT JOIN transactions t ON t.from_account_id = a.id OR t.to_account_id = a.id
WHERE a.archived_at IS NULL GROUP BY a.id;
SELECT a.name,
  a.opening_balance_minor
    + COALESCE(SUM(CASE WHEN t.to_account_id   = a.id THEN t.amount_minor ELSE 0 END), 0)
    - COALESCE(SUM(CASE WHEN t.from_account_id = a.id THEN t.amount_minor ELSE 0 END), 0)
FROM accounts a
LEFT JOIN transactions t ON t.from_account_id = a.id OR t.to_account_id = a.id
WHERE a.archived_at IS NULL GROUP BY a.id;
SELECT a.name,
  a.opening_balance_minor
    + COALESCE(SUM(CASE WHEN t.to_account_id   = a.id THEN t.amount_minor ELSE 0 END), 0)
    - COALESCE(SUM(CASE WHEN t.from_account_id = a.id THEN t.amount_minor ELSE 0 END), 0)
FROM accounts a
LEFT JOIN transactions t ON t.from_account_id = a.id OR t.to_account_id = a.id
WHERE a.archived_at IS NULL GROUP BY a.id;
