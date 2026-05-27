-- UI-driven queries. Run via: ./run.sh queries
-- Each section is what one screen needs.
--
-- Convention: amount_minor is paise. Display formula: printf('%,.2f', amount_minor/100.0)
-- but SQLite's printf doesn't do thousand separators, so we'll use plain decimal.

.headers on
.mode column
.width 0 0 0 0 0 0 0

.print
.print ═══════════════════════════════════════════════════════════════
.print  HOME · account balances (all accounts, with computed balance)
.print ═══════════════════════════════════════════════════════════════

WITH balances AS (
  SELECT
    a.id,
    a.name,
    a.type,
    a.parent_account_id,
    a.opening_balance_minor
      + COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id), 0)
      - COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id), 0)
      AS balance_minor
  FROM accounts a
  WHERE a.archived_at IS NULL
)
SELECT
  type      AS type,
  name      AS account,
  printf('%.2f', balance_minor / 100.0) AS balance_pkr,
  CASE WHEN balance_minor < 0 THEN 'WARN' ELSE '' END AS flag
FROM balances
ORDER BY type DESC, name;

.print
.print --- total (headline on Home)
-- Total = sum of every non-archived account, REAL + BUDGET.
-- Borrowed money is included (it's spendable); debts are never netted here.
SELECT
  printf('%.2f', SUM(balance_minor) / 100.0)                                       AS total,
  printf('%.2f', SUM(CASE WHEN type='REAL'   THEN balance_minor ELSE 0 END) / 100.0) AS unallocated_real,
  printf('%.2f', SUM(CASE WHEN type='BUDGET' THEN balance_minor ELSE 0 END) / 100.0) AS in_budgets
FROM (
  SELECT
    a.type,
    a.opening_balance_minor
      + COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE to_account_id   = a.id), 0)
      - COALESCE((SELECT SUM(amount_minor) FROM transactions WHERE from_account_id = a.id), 0)
      AS balance_minor
  FROM accounts a
  WHERE a.archived_at IS NULL
);

.print
.print ═══════════════════════════════════════════════════════════════
.print  HOME · pending recurring (due or overdue)
.print ═══════════════════════════════════════════════════════════════

SELECT
  rs.name,
  printf('%.2f', rs.amount_minor / 100.0) AS amount,
  a.name AS from_account,
  date(rs.next_due_at / 1000, 'unixepoch') AS due,
  CAST((strftime('%s','2026-05-27') * 1000 - rs.next_due_at) / 86400000.0 AS INTEGER) AS days_overdue
FROM recurring_schedules rs
LEFT JOIN accounts a ON a.id = rs.from_account_id
WHERE rs.status = 'ACTIVE'
  AND rs.next_due_at <= strftime('%s','2026-05-27') * 1000
ORDER BY rs.next_due_at;

.print
.print ═══════════════════════════════════════════════════════════════
.print  HOME · recent transactions (last 10, with display fields)
.print ═══════════════════════════════════════════════════════════════

SELECT
  date(t.recorded_at / 1000, 'unixepoch') AS day,
  -- Title: best human-readable label, never a raw kind string.
  COALESCE(
    t.note,
    CASE t.kind
      WHEN 'INCOME'         THEN c.name
      WHEN 'EXPENSE'        THEN c.name
      WHEN 'TRANSFER'       THEN 'Transfer'
      WHEN 'ALLOCATION'     THEN 'Allocated to ' || ato.name
      WHEN 'REALLOCATION'   THEN 'Moved to ' || ato.name
      WHEN 'DEBT_BORROW'    THEN 'Borrowed from ' || p.name
      WHEN 'DEBT_LEND'      THEN 'Lent to ' || p.name
      WHEN 'DEBT_REPAY_OUT' THEN 'Repaid ' || p.name
      WHEN 'DEBT_REPAY_IN'  THEN 'Received from ' || p.name
    END
  ) AS title,
  -- Subtitle: account context.
  CASE t.kind
    WHEN 'INCOME'         THEN ato.name
    WHEN 'EXPENSE'        THEN afrom.name
    WHEN 'TRANSFER'       THEN afrom.name || ' → ' || ato.name
    WHEN 'ALLOCATION'     THEN afrom.name || ' → ' || ato.name
    WHEN 'REALLOCATION'   THEN afrom.name || ' → ' || ato.name
    WHEN 'DEBT_BORROW'    THEN '→ ' || ato.name
    WHEN 'DEBT_LEND'      THEN afrom.name || ' →'
    WHEN 'DEBT_REPAY_OUT' THEN afrom.name || ' →'
    WHEN 'DEBT_REPAY_IN'  THEN '→ ' || ato.name
  END AS subtitle,
  -- Signed amount from the user's-money perspective.
  CASE
    WHEN t.kind IN ('INCOME','DEBT_BORROW','DEBT_REPAY_IN')  THEN '+'
    WHEN t.kind IN ('EXPENSE','DEBT_LEND','DEBT_REPAY_OUT')  THEN '-'
    ELSE '↔'
  END || printf('%.2f', t.amount_minor / 100.0) AS amount
FROM transactions t
LEFT JOIN accounts   afrom ON afrom.id = t.from_account_id
LEFT JOIN accounts   ato   ON ato.id   = t.to_account_id
LEFT JOIN categories c     ON c.id     = t.category_id
LEFT JOIN debts      d     ON d.id     = t.debt_id
LEFT JOIN persons    p     ON p.id     = d.person_id
ORDER BY t.recorded_at DESC, t.created_at DESC
LIMIT 10;

.print
.print ═══════════════════════════════════════════════════════════════
.print  ACCOUNT DETAIL · Bank — history
.print ═══════════════════════════════════════════════════════════════

SELECT
  date(t.recorded_at / 1000, 'unixepoch') AS day,
  COALESCE(
    t.note,
    CASE t.kind
      WHEN 'INCOME'         THEN c.name
      WHEN 'EXPENSE'        THEN c.name
      WHEN 'TRANSFER'       THEN 'Transfer'
      WHEN 'ALLOCATION'     THEN 'Allocated to ' || ato.name
      WHEN 'REALLOCATION'   THEN 'Moved to ' || ato.name
      WHEN 'DEBT_BORROW'    THEN 'Borrowed from ' || p.name
      WHEN 'DEBT_LEND'      THEN 'Lent to ' || p.name
      WHEN 'DEBT_REPAY_OUT' THEN 'Repaid ' || p.name
      WHEN 'DEBT_REPAY_IN'  THEN 'Received from ' || p.name
    END
  ) AS title,
  -- Sign is from THIS account's perspective.
  CASE WHEN t.to_account_id = X'A1' THEN '+' ELSE '-' END
    || printf('%.2f', t.amount_minor / 100.0) AS delta
FROM transactions t
LEFT JOIN accounts   ato ON ato.id = t.to_account_id
LEFT JOIN categories c   ON c.id   = t.category_id
LEFT JOIN debts      d   ON d.id   = t.debt_id
LEFT JOIN persons    p   ON p.id   = d.person_id
WHERE t.from_account_id = X'A1' OR t.to_account_id = X'A1'
ORDER BY t.recorded_at DESC, t.created_at DESC;

.print
.print ═══════════════════════════════════════════════════════════════
.print  RECURRING · all schedules
.print ═══════════════════════════════════════════════════════════════

SELECT
  rs.name,
  printf('%.2f', rs.amount_minor / 100.0) AS amount,
  rs.cadence,
  COALESCE(rs.day_of_week, rs.day_of_month) AS day,
  date(rs.next_due_at / 1000, 'unixepoch')   AS next_due,
  rs.status,
  (SELECT COUNT(*) FROM transactions WHERE recurring_id = rs.id) AS times_fired
FROM recurring_schedules rs
ORDER BY rs.next_due_at;

.print
.print ═══════════════════════════════════════════════════════════════
.print  DEBTS · I OWE tab
.print ═══════════════════════════════════════════════════════════════

WITH debt_paid AS (
  SELECT debt_id, SUM(amount_minor) AS paid_minor
  FROM transactions
  WHERE kind IN ('DEBT_REPAY_OUT','DEBT_REPAY_IN')
  GROUP BY debt_id
)
SELECT
  p.name AS person,
  printf('%.2f', d.original_amount_minor / 100.0)                    AS original,
  printf('%.2f', COALESCE(dp.paid_minor, 0) / 100.0)                 AS paid,
  printf('%.2f', (d.original_amount_minor - COALESCE(dp.paid_minor, 0)) / 100.0) AS remaining,
  d.reason,
  d.status,
  date(d.created_at / 1000, 'unixepoch') AS since
FROM debts d
JOIN persons p ON p.id = d.person_id
LEFT JOIN debt_paid dp ON dp.debt_id = d.id
WHERE d.direction = 'I_OWE'
ORDER BY d.status, d.created_at DESC;

.print
.print ═══════════════════════════════════════════════════════════════
.print  DEBTS · OWED TO ME tab
.print ═══════════════════════════════════════════════════════════════

WITH debt_paid AS (
  SELECT debt_id, SUM(amount_minor) AS paid_minor
  FROM transactions
  WHERE kind IN ('DEBT_REPAY_OUT','DEBT_REPAY_IN')
  GROUP BY debt_id
)
SELECT
  p.name AS person,
  printf('%.2f', d.original_amount_minor / 100.0)                    AS original,
  printf('%.2f', COALESCE(dp.paid_minor, 0) / 100.0)                 AS received,
  printf('%.2f', (d.original_amount_minor - COALESCE(dp.paid_minor, 0)) / 100.0) AS remaining,
  d.reason,
  d.status
FROM debts d
JOIN persons p ON p.id = d.person_id
LEFT JOIN debt_paid dp ON dp.debt_id = d.id
WHERE d.direction = 'OWED_TO_ME'
ORDER BY d.status, d.created_at DESC;

.print
.print ═══════════════════════════════════════════════════════════════
.print  REPORTS · May 2026 — income / expense / net
.print ═══════════════════════════════════════════════════════════════

WITH period AS (
  SELECT
    strftime('%s','2026-05-01') * 1000 AS start_ms,
    strftime('%s','2026-06-01') * 1000 AS end_ms
)
SELECT
  printf('%.2f',
    (SELECT COALESCE(SUM(amount_minor), 0)
       FROM transactions, period
       WHERE kind = 'INCOME'
         AND recorded_at >= start_ms AND recorded_at < end_ms) / 100.0
  ) AS income,
  printf('%.2f',
    (SELECT COALESCE(SUM(amount_minor), 0)
       FROM transactions, period
       WHERE kind = 'EXPENSE'
         AND recorded_at >= start_ms AND recorded_at < end_ms) / 100.0
  ) AS expense,
  printf('%.2f',
    ((SELECT COALESCE(SUM(amount_minor), 0)
        FROM transactions, period
        WHERE kind = 'INCOME'
          AND recorded_at >= start_ms AND recorded_at < end_ms)
     -
     (SELECT COALESCE(SUM(amount_minor), 0)
        FROM transactions, period
        WHERE kind = 'EXPENSE'
          AND recorded_at >= start_ms AND recorded_at < end_ms)) / 100.0
  ) AS net;

.print
.print --- expense by category (May 2026)
SELECT
  c.name AS category,
  printf('%.2f', SUM(t.amount_minor) / 100.0) AS total,
  COUNT(*) AS txns
FROM transactions t
JOIN categories c ON c.id = t.category_id
WHERE t.kind = 'EXPENSE'
  AND t.recorded_at >= strftime('%s','2026-05-01') * 1000
  AND t.recorded_at <  strftime('%s','2026-06-01') * 1000
GROUP BY c.id
ORDER BY SUM(t.amount_minor) DESC;
