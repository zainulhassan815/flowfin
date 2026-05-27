-- Seed data: Ali, the PRD's primary persona.
-- One month of activity ending May 26 2026. Today (per session) = May 27 2026.
--
-- ID conventions (BLOB literals for readability — prod uses UUIDv7 BLOB(16)):
--   accounts:   X'A1' Bank, X'A2' Cash, X'A3' JazzCash,
--               X'B1' Food, X'B2' Transport, X'B3' Entertainment
--   categories: X'C1' Salary, X'C2' Food, X'C3' Groceries, X'C4' Rent,
--               X'C5' Transport, X'C6' Subscriptions, X'C7' Debt Repayment
--   persons:    X'D1' Ahmed, X'D2' Sara
--   debts:      X'E1' Ahmed/I_OWE, X'E2' Sara/OWED_TO_ME
--   recurring:  X'F1' Netflix, X'F2' Gym
--   tx:         X'01'..

PRAGMA foreign_keys = ON;

------------------------------------------------------------------------------
-- Defaults
------------------------------------------------------------------------------
-- Reference time used for created_at/updated_at on seed rows.
-- All timestamps are epoch ms.

-- Categories (defaults)
INSERT INTO categories (id, name, scope, is_default, icon, color, display_order, created_at, updated_at) VALUES
  (X'C1', 'Salary',         'INCOME',  1, 'wallet',     '#9CD4A2', 1, strftime('%s','2026-01-01')*1000, strftime('%s','2026-01-01')*1000),
  (X'C2', 'Food & Dining',  'EXPENSE', 1, 'fork',       '#E8B66E', 1, strftime('%s','2026-01-01')*1000, strftime('%s','2026-01-01')*1000),
  (X'C3', 'Groceries',      'EXPENSE', 1, 'cart',       '#E8B66E', 2, strftime('%s','2026-01-01')*1000, strftime('%s','2026-01-01')*1000),
  (X'C4', 'Rent',           'EXPENSE', 1, 'home',       '#E08A8A', 3, strftime('%s','2026-01-01')*1000, strftime('%s','2026-01-01')*1000),
  (X'C5', 'Transport',      'EXPENSE', 1, 'car',        '#82C5D4', 4, strftime('%s','2026-01-01')*1000, strftime('%s','2026-01-01')*1000),
  (X'C6', 'Subscriptions',  'EXPENSE', 1, 'broadcast',  '#B89CD4', 5, strftime('%s','2026-01-01')*1000, strftime('%s','2026-01-01')*1000),
  (X'C7', 'Debt Repayment', 'EXPENSE', 1, 'handshake',  '#8A8A92', 6, strftime('%s','2026-01-01')*1000, strftime('%s','2026-01-01')*1000);

-- Accounts: Real first (so the BUDGET parent triggers can find them).
INSERT INTO accounts (id, name, type, currency, parent_account_id, opening_balance_minor, color, icon, display_order, created_at, updated_at) VALUES
  (X'A1', 'Bank',     'REAL', 'PKR', NULL,  10000000, '#82C5D4', 'bank',     1, strftime('%s','2026-04-01')*1000, strftime('%s','2026-04-01')*1000),
  (X'A2', 'Cash',     'REAL', 'PKR', NULL,    500000, '#9CD4A2', 'cash',     2, strftime('%s','2026-04-01')*1000, strftime('%s','2026-04-01')*1000),
  (X'A3', 'JazzCash', 'REAL', 'PKR', NULL,    200000, '#E8B66E', 'phone',    3, strftime('%s','2026-04-01')*1000, strftime('%s','2026-04-01')*1000);

INSERT INTO accounts (id, name, type, currency, parent_account_id, opening_balance_minor, color, icon, display_order, created_at, updated_at) VALUES
  (X'B1', 'Food',          'BUDGET', 'PKR', X'A1', 0, '#E8B66E', 'fork',  4, strftime('%s','2026-05-01')*1000, strftime('%s','2026-05-01')*1000),
  (X'B2', 'Transport',     'BUDGET', 'PKR', X'A1', 0, '#82C5D4', 'car',   5, strftime('%s','2026-05-01')*1000, strftime('%s','2026-05-01')*1000),
  (X'B3', 'Entertainment', 'BUDGET', 'PKR', X'A1', 0, '#B89CD4', 'game',  6, strftime('%s','2026-05-01')*1000, strftime('%s','2026-05-01')*1000);

-- Persons
INSERT INTO persons (id, name, avatar_tint_index, created_at, updated_at) VALUES
  (X'D1', 'Ahmed', 1, strftime('%s','2026-05-08')*1000, strftime('%s','2026-05-08')*1000),
  (X'D2', 'Sara',  3, strftime('%s','2026-05-15')*1000, strftime('%s','2026-05-15')*1000);

-- Recurring schedules. Netflix already fired once (May 5); next due = June 5.
-- Gym is overdue: was due May 5, never paid → still ACTIVE with next_due_at in the past.
INSERT INTO recurring_schedules (id, name, amount_minor, from_account_id, to_account_id, category_id, cadence, day_of_week, day_of_month, month_of_year, next_due_at, status, created_at, updated_at) VALUES
  (X'F1', 'Netflix', 150000, X'A1', NULL, X'C6', 'MONTHLY', NULL, 5, NULL, strftime('%s','2026-06-05')*1000, 'ACTIVE', strftime('%s','2026-05-01')*1000, strftime('%s','2026-05-05')*1000),
  (X'F2', 'Gym',     500000, X'A1', NULL, X'C6', 'MONTHLY', NULL, 5, NULL, strftime('%s','2026-05-05')*1000, 'ACTIVE', strftime('%s','2026-05-01')*1000, strftime('%s','2026-05-01')*1000);

------------------------------------------------------------------------------
-- Transactions
------------------------------------------------------------------------------

-- May 1: Salary 150,000 → Bank
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'01', 'INCOME', NULL, X'A1', 15000000, X'C1', 'May salary', strftime('%s','2026-05-01')*1000, NULL, NULL, strftime('%s','2026-05-01')*1000, strftime('%s','2026-05-01')*1000);

-- May 2: Allocate to budgets
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'02', 'ALLOCATION', X'A1', X'B1', 1500000, NULL, NULL, strftime('%s','2026-05-02')*1000, NULL, NULL, strftime('%s','2026-05-02')*1000, strftime('%s','2026-05-02')*1000),
  (X'03', 'ALLOCATION', X'A1', X'B2',  500000, NULL, NULL, strftime('%s','2026-05-02')*1000, NULL, NULL, strftime('%s','2026-05-02')*1000, strftime('%s','2026-05-02')*1000),
  (X'04', 'ALLOCATION', X'A1', X'B3',  300000, NULL, NULL, strftime('%s','2026-05-02')*1000, NULL, NULL, strftime('%s','2026-05-02')*1000, strftime('%s','2026-05-02')*1000);

-- May 3: Rent 30,000 from Bank
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'05', 'EXPENSE', X'A1', NULL, 3000000, X'C4', 'May rent', strftime('%s','2026-05-03')*1000, NULL, NULL, strftime('%s','2026-05-03')*1000, strftime('%s','2026-05-03')*1000);

-- May 5: Netflix recurring fires (1,500 from Bank, recurring_id = F1)
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'06', 'EXPENSE', X'A1', NULL, 150000, X'C6', 'Netflix', strftime('%s','2026-05-05')*1000, X'F1', NULL, strftime('%s','2026-05-05')*1000, strftime('%s','2026-05-05')*1000);

-- May 8: Borrow 8,000 from Ahmed (cycle insert: tx + debt in one transaction).
BEGIN;
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'07', 'DEBT_BORROW', NULL, X'A1', 800000, NULL, 'Borrowed from Ahmed', strftime('%s','2026-05-08')*1000, NULL, X'E1', strftime('%s','2026-05-08')*1000, strftime('%s','2026-05-08')*1000);
INSERT INTO debts (id, person_id, direction, original_amount_minor, currency, reason, status, origin_transaction_id, created_at, updated_at) VALUES
  (X'E1', X'D1', 'I_OWE', 800000, 'PKR', 'Rent help', 'ACTIVE', X'07', strftime('%s','2026-05-08')*1000, strftime('%s','2026-05-08')*1000);
COMMIT;

-- May 10: ATM withdrawal — Bank → Cash 3,000
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'08', 'TRANSFER', X'A1', X'A2', 300000, NULL, 'ATM withdrawal', strftime('%s','2026-05-10')*1000, NULL, NULL, strftime('%s','2026-05-10')*1000, strftime('%s','2026-05-10')*1000);

-- May 12: Lunch from Food 500
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'09', 'EXPENSE', X'B1', NULL, 50000, X'C2', 'Mess lunch', strftime('%s','2026-05-12')*1000, NULL, NULL, strftime('%s','2026-05-12')*1000, strftime('%s','2026-05-12')*1000);

-- May 14: Rickshaw 200 from Cash (transport category)
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'0A', 'EXPENSE', X'A2', NULL, 20000, X'C5', 'Rickshaw', strftime('%s','2026-05-14')*1000, NULL, NULL, strftime('%s','2026-05-14')*1000, strftime('%s','2026-05-14')*1000);

-- May 15: Lend 1,000 to Sara
BEGIN;
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'0B', 'DEBT_LEND', X'A1', NULL, 100000, NULL, 'Lent to Sara', strftime('%s','2026-05-15')*1000, NULL, X'E2', strftime('%s','2026-05-15')*1000, strftime('%s','2026-05-15')*1000);
INSERT INTO debts (id, person_id, direction, original_amount_minor, currency, reason, status, origin_transaction_id, created_at, updated_at) VALUES
  (X'E2', X'D2', 'OWED_TO_ME', 100000, 'PKR', 'Lunch split cover', 'ACTIVE', X'0B', strftime('%s','2026-05-15')*1000, strftime('%s','2026-05-15')*1000);
COMMIT;

-- May 18: Another lunch from Food 600
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'0C', 'EXPENSE', X'B1', NULL, 60000, X'C2', 'Karahi', strftime('%s','2026-05-18')*1000, NULL, NULL, strftime('%s','2026-05-18')*1000, strftime('%s','2026-05-18')*1000);

-- May 20: Repay Ahmed 2,000 from Bank
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'0D', 'DEBT_REPAY_OUT', X'A1', NULL, 200000, NULL, 'To Ahmed', strftime('%s','2026-05-20')*1000, NULL, X'E1', strftime('%s','2026-05-20')*1000, strftime('%s','2026-05-20')*1000);

-- May 22: Reallocate 500 from Entertainment → Food
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'0E', 'REALLOCATION', X'B3', X'B1', 50000, NULL, 'Topping up Food', strftime('%s','2026-05-22')*1000, NULL, NULL, strftime('%s','2026-05-22')*1000, strftime('%s','2026-05-22')*1000);

-- May 25: Groceries 2,500 from Bank
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'0F', 'EXPENSE', X'A1', NULL, 250000, X'C3', 'Imtiaz run', strftime('%s','2026-05-25')*1000, NULL, NULL, strftime('%s','2026-05-25')*1000, strftime('%s','2026-05-25')*1000);

-- May 26: Lunch from Food 450
INSERT INTO transactions (id, kind, from_account_id, to_account_id, amount_minor, category_id, note, recorded_at, recurring_id, debt_id, created_at, updated_at) VALUES
  (X'10', 'EXPENSE', X'B1', NULL, 45000, X'C2', 'Daal chawal', strftime('%s','2026-05-26')*1000, NULL, NULL, strftime('%s','2026-05-26')*1000, strftime('%s','2026-05-26')*1000);
