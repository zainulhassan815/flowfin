-- FlowFin v0 schema — experimentation copy.
-- Targets SQLite 3.37+. Build with: sqlite3 flowfin.db < schema.sql

PRAGMA foreign_keys = ON;

------------------------------------------------------------------------------
-- accounts
------------------------------------------------------------------------------
CREATE TABLE accounts (
  id                       BLOB    PRIMARY KEY,
  name                     TEXT    NOT NULL,
  type                     TEXT    NOT NULL CHECK (type IN ('REAL','BUDGET')),
  currency                 TEXT    NOT NULL DEFAULT 'PKR',
  parent_account_id        BLOB    REFERENCES accounts(id) ON DELETE RESTRICT,
  opening_balance_minor    INTEGER NOT NULL DEFAULT 0,
  color                    TEXT,
  icon                     TEXT,
  display_order            INTEGER NOT NULL DEFAULT 0,
  created_at               INTEGER NOT NULL,
  updated_at               INTEGER NOT NULL,
  archived_at              INTEGER,
  CHECK (
    (type = 'REAL'   AND parent_account_id IS NULL)
    OR
    (type = 'BUDGET' AND parent_account_id IS NOT NULL)
  )
);

CREATE UNIQUE INDEX accounts_name_unique
  ON accounts(name)
  WHERE archived_at IS NULL;

-- A budget's parent must be a REAL account (CHECK can't subquery).
CREATE TRIGGER accounts_parent_must_be_real_ins
BEFORE INSERT ON accounts
WHEN NEW.parent_account_id IS NOT NULL
BEGIN
  SELECT CASE
    WHEN (SELECT type FROM accounts WHERE id = NEW.parent_account_id) != 'REAL'
    THEN RAISE(ABORT, 'budget parent must be REAL')
  END;
END;

CREATE TRIGGER accounts_parent_must_be_real_upd
BEFORE UPDATE OF parent_account_id ON accounts
WHEN NEW.parent_account_id IS NOT NULL
BEGIN
  SELECT CASE
    WHEN (SELECT type FROM accounts WHERE id = NEW.parent_account_id) != 'REAL'
    THEN RAISE(ABORT, 'budget parent must be REAL')
  END;
END;

-- A budget's currency must match its parent's.
CREATE TRIGGER accounts_currency_matches_parent_ins
BEFORE INSERT ON accounts
WHEN NEW.parent_account_id IS NOT NULL
BEGIN
  SELECT CASE
    WHEN (SELECT currency FROM accounts WHERE id = NEW.parent_account_id) != NEW.currency
    THEN RAISE(ABORT, 'budget currency must match parent')
  END;
END;

------------------------------------------------------------------------------
-- categories
------------------------------------------------------------------------------
CREATE TABLE categories (
  id              BLOB    PRIMARY KEY,
  name            TEXT    NOT NULL,
  scope           TEXT    NOT NULL CHECK (scope IN ('EXPENSE','INCOME')),
  is_default      INTEGER NOT NULL DEFAULT 0 CHECK (is_default IN (0,1)),
  icon            TEXT,
  color           TEXT,
  display_order   INTEGER NOT NULL DEFAULT 0,
  created_at      INTEGER NOT NULL,
  updated_at      INTEGER NOT NULL,
  archived_at     INTEGER,
  CHECK (NOT (is_default = 1 AND archived_at IS NOT NULL))
);

------------------------------------------------------------------------------
-- persons
------------------------------------------------------------------------------
CREATE TABLE persons (
  id                  BLOB    PRIMARY KEY,
  name                TEXT    NOT NULL,
  avatar_tint_index   INTEGER NOT NULL DEFAULT 1 CHECK (avatar_tint_index BETWEEN 1 AND 5),
  created_at          INTEGER NOT NULL,
  updated_at          INTEGER NOT NULL,
  archived_at         INTEGER
);

CREATE UNIQUE INDEX persons_name_unique
  ON persons(name COLLATE NOCASE)
  WHERE archived_at IS NULL;

------------------------------------------------------------------------------
-- recurring_schedules
------------------------------------------------------------------------------
CREATE TABLE recurring_schedules (
  id               BLOB    PRIMARY KEY,
  name             TEXT    NOT NULL,
  amount_minor     INTEGER NOT NULL CHECK (amount_minor > 0),
  from_account_id  BLOB    REFERENCES accounts(id)   ON DELETE RESTRICT,
  to_account_id    BLOB    REFERENCES accounts(id)   ON DELETE RESTRICT,
  category_id      BLOB    REFERENCES categories(id) ON DELETE RESTRICT,
  cadence          TEXT    NOT NULL CHECK (cadence IN ('WEEKLY','MONTHLY','YEARLY')),
  day_of_week      INTEGER CHECK (day_of_week  BETWEEN 1 AND 7),
  day_of_month     INTEGER CHECK (day_of_month BETWEEN 1 AND 31),
  month_of_year    INTEGER CHECK (month_of_year BETWEEN 1 AND 12),
  next_due_at      INTEGER NOT NULL,
  status           TEXT    NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','PAUSED')),
  paused_at        INTEGER,
  created_at       INTEGER NOT NULL,
  updated_at       INTEGER NOT NULL,
  CHECK (
    (cadence = 'WEEKLY'  AND day_of_week IS NOT NULL  AND day_of_month IS NULL     AND month_of_year IS NULL)
    OR
    (cadence = 'MONTHLY' AND day_of_week IS NULL      AND day_of_month IS NOT NULL AND month_of_year IS NULL)
    OR
    (cadence = 'YEARLY'  AND day_of_week IS NULL      AND day_of_month IS NOT NULL AND month_of_year IS NOT NULL)
  )
);

CREATE INDEX recurring_due ON recurring_schedules(status, next_due_at);

------------------------------------------------------------------------------
-- debts
-- origin_transaction_id is a deferred FK so a debt and its origin transaction
-- can be inserted inside a single BEGIN..COMMIT.
------------------------------------------------------------------------------
CREATE TABLE debts (
  id                       BLOB    PRIMARY KEY,
  person_id                BLOB    NOT NULL REFERENCES persons(id) ON DELETE RESTRICT,
  direction                TEXT    NOT NULL CHECK (direction IN ('I_OWE','OWED_TO_ME')),
  original_amount_minor    INTEGER NOT NULL CHECK (original_amount_minor > 0),
  currency                 TEXT    NOT NULL,
  reason                   TEXT,
  status                   TEXT    NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SETTLED')),
  origin_transaction_id    BLOB    NOT NULL,
  created_at               INTEGER NOT NULL,
  updated_at               INTEGER NOT NULL,
  settled_at               INTEGER,
  FOREIGN KEY (origin_transaction_id) REFERENCES transactions(id) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX debts_status_person ON debts(status, person_id);

------------------------------------------------------------------------------
-- transactions
------------------------------------------------------------------------------
CREATE TABLE transactions (
  id                 BLOB    PRIMARY KEY,
  kind               TEXT    NOT NULL CHECK (kind IN (
                       'INCOME','EXPENSE','TRANSFER',
                       'ALLOCATION','REALLOCATION',
                       'DEBT_BORROW','DEBT_LEND',
                       'DEBT_REPAY_OUT','DEBT_REPAY_IN'
                     )),
  from_account_id    BLOB    REFERENCES accounts(id)            ON DELETE RESTRICT,
  to_account_id      BLOB    REFERENCES accounts(id)            ON DELETE RESTRICT,
  amount_minor       INTEGER NOT NULL CHECK (amount_minor > 0),
  category_id        BLOB    REFERENCES categories(id)          ON DELETE SET NULL,
  note               TEXT,
  recorded_at        INTEGER NOT NULL,
  recurring_id       BLOB    REFERENCES recurring_schedules(id) ON DELETE SET NULL,
  debt_id            BLOB,
  created_at         INTEGER NOT NULL,
  updated_at         INTEGER NOT NULL,
  FOREIGN KEY (debt_id) REFERENCES debts(id) DEFERRABLE INITIALLY DEFERRED,

  -- Per-kind nullability shape.
  CHECK (
    (kind = 'INCOME'         AND from_account_id IS NULL     AND to_account_id IS NOT NULL AND category_id IS NOT NULL AND debt_id IS NULL)
    OR (kind = 'EXPENSE'         AND from_account_id IS NOT NULL AND to_account_id IS NULL     AND category_id IS NOT NULL AND debt_id IS NULL)
    OR (kind = 'TRANSFER'        AND from_account_id IS NOT NULL AND to_account_id IS NOT NULL AND category_id IS NULL     AND debt_id IS NULL)
    OR (kind = 'ALLOCATION'      AND from_account_id IS NOT NULL AND to_account_id IS NOT NULL AND category_id IS NULL     AND debt_id IS NULL)
    OR (kind = 'REALLOCATION'    AND from_account_id IS NOT NULL AND to_account_id IS NOT NULL AND category_id IS NULL     AND debt_id IS NULL)
    OR (kind = 'DEBT_BORROW'     AND from_account_id IS NULL     AND to_account_id IS NOT NULL AND category_id IS NULL     AND debt_id IS NOT NULL)
    OR (kind = 'DEBT_LEND'       AND from_account_id IS NOT NULL AND to_account_id IS NULL     AND category_id IS NULL     AND debt_id IS NOT NULL)
    OR (kind = 'DEBT_REPAY_OUT'  AND from_account_id IS NOT NULL AND to_account_id IS NULL     AND category_id IS NULL     AND debt_id IS NOT NULL)
    OR (kind = 'DEBT_REPAY_IN'   AND from_account_id IS NULL     AND to_account_id IS NOT NULL AND category_id IS NULL     AND debt_id IS NOT NULL)
  ),

  CHECK (NOT (recurring_id IS NOT NULL AND debt_id IS NOT NULL)),
  CHECK ((kind IN ('INCOME','EXPENSE')) OR recurring_id IS NULL),

  -- D1: no self-loops.
  CHECK (from_account_id IS NULL OR to_account_id IS NULL OR from_account_id != to_account_id)
);

CREATE INDEX tx_recorded_at       ON transactions(recorded_at DESC);
CREATE INDEX tx_from_recorded     ON transactions(from_account_id, recorded_at DESC);
CREATE INDEX tx_to_recorded       ON transactions(to_account_id,   recorded_at DESC);
CREATE INDEX tx_category_recorded ON transactions(category_id, recorded_at DESC) WHERE category_id IS NOT NULL;
CREATE INDEX tx_debt              ON transactions(debt_id)      WHERE debt_id IS NOT NULL;
CREATE INDEX tx_recurring         ON transactions(recurring_id) WHERE recurring_id IS NOT NULL;
CREATE INDEX tx_kind_recorded     ON transactions(kind, recorded_at DESC);

------------------------------------------------------------------------------
-- transactions: cross-row invariant triggers
------------------------------------------------------------------------------

-- D2: both endpoints must share currency.
CREATE TRIGGER tx_currency_match_ins
BEFORE INSERT ON transactions
WHEN NEW.from_account_id IS NOT NULL AND NEW.to_account_id IS NOT NULL
BEGIN
  SELECT CASE
    WHEN (SELECT currency FROM accounts WHERE id = NEW.from_account_id)
       != (SELECT currency FROM accounts WHERE id = NEW.to_account_id)
    THEN RAISE(ABORT, 'currency mismatch between accounts')
  END;
END;

CREATE TRIGGER tx_currency_match_upd
BEFORE UPDATE OF from_account_id, to_account_id ON transactions
WHEN NEW.from_account_id IS NOT NULL AND NEW.to_account_id IS NOT NULL
BEGIN
  SELECT CASE
    WHEN (SELECT currency FROM accounts WHERE id = NEW.from_account_id)
       != (SELECT currency FROM accounts WHERE id = NEW.to_account_id)
    THEN RAISE(ABORT, 'currency mismatch between accounts')
  END;
END;

-- D3: category scope must match kind.
CREATE TRIGGER tx_category_scope_ins
BEFORE INSERT ON transactions
WHEN NEW.category_id IS NOT NULL
BEGIN
  SELECT CASE
    WHEN NEW.kind = 'INCOME'  AND (SELECT scope FROM categories WHERE id = NEW.category_id) != 'INCOME'
      THEN RAISE(ABORT, 'INCOME row needs INCOME-scope category')
    WHEN NEW.kind = 'EXPENSE' AND (SELECT scope FROM categories WHERE id = NEW.category_id) != 'EXPENSE'
      THEN RAISE(ABORT, 'EXPENSE row needs EXPENSE-scope category')
  END;
END;

CREATE TRIGGER tx_category_scope_upd
BEFORE UPDATE OF kind, category_id ON transactions
WHEN NEW.category_id IS NOT NULL
BEGIN
  SELECT CASE
    WHEN NEW.kind = 'INCOME'  AND (SELECT scope FROM categories WHERE id = NEW.category_id) != 'INCOME'
      THEN RAISE(ABORT, 'INCOME row needs INCOME-scope category')
    WHEN NEW.kind = 'EXPENSE' AND (SELECT scope FROM categories WHERE id = NEW.category_id) != 'EXPENSE'
      THEN RAISE(ABORT, 'EXPENSE row needs EXPENSE-scope category')
  END;
END;

-- D4: REALLOCATION must stay within one real parent.
CREATE TRIGGER tx_reallocation_same_parent_ins
BEFORE INSERT ON transactions
WHEN NEW.kind = 'REALLOCATION'
BEGIN
  SELECT CASE
    WHEN (SELECT parent_account_id FROM accounts WHERE id = NEW.from_account_id)
       != (SELECT parent_account_id FROM accounts WHERE id = NEW.to_account_id)
    THEN RAISE(ABORT, 'reallocation must stay within the same real parent')
  END;
END;

-- D7: cannot write a new transaction whose recorded_at is after an
-- endpoint account's archived_at. (Back-dating an old transaction whose
-- recorded_at is before archival is still allowed.)
CREATE TRIGGER tx_no_writes_to_archived_ins
BEFORE INSERT ON transactions
BEGIN
  SELECT CASE
    WHEN NEW.from_account_id IS NOT NULL AND
         (SELECT archived_at FROM accounts WHERE id = NEW.from_account_id) IS NOT NULL AND
         NEW.recorded_at > (SELECT archived_at FROM accounts WHERE id = NEW.from_account_id)
    THEN RAISE(ABORT, 'cannot write to archived from_account')
    WHEN NEW.to_account_id IS NOT NULL AND
         (SELECT archived_at FROM accounts WHERE id = NEW.to_account_id) IS NOT NULL AND
         NEW.recorded_at > (SELECT archived_at FROM accounts WHERE id = NEW.to_account_id)
    THEN RAISE(ABORT, 'cannot write to archived to_account')
  END;
END;

------------------------------------------------------------------------------
-- debts: cross-row invariant trigger
------------------------------------------------------------------------------

-- D6: debt.currency must match the origin transaction's account currency.
CREATE TRIGGER debts_currency_match_ins
BEFORE INSERT ON debts
BEGIN
  SELECT CASE
    WHEN NEW.currency != (
      SELECT COALESCE(afrom.currency, ato.currency)
      FROM transactions t
      LEFT JOIN accounts afrom ON afrom.id = t.from_account_id
      LEFT JOIN accounts ato   ON ato.id   = t.to_account_id
      WHERE t.id = NEW.origin_transaction_id
    )
    THEN RAISE(ABORT, 'debt currency must match origin transaction account')
  END;
END;

------------------------------------------------------------------------------
-- schema_meta
------------------------------------------------------------------------------
CREATE TABLE schema_meta (
  key   TEXT NOT NULL PRIMARY KEY,
  value TEXT NOT NULL
);
INSERT INTO schema_meta VALUES ('version', '1');
INSERT INTO schema_meta VALUES ('device_id', lower(hex(randomblob(16))));
