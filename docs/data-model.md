# FlowFin — data model

Six tables. Money is a single ledger (`transactions`); everything else is a relationship pointing at it. Balances are computed.

## Principles

1. **Money is one table.** Every cash event is a row in `transactions`.
2. **Relationships live separately.** A debt is a record about a person; the payments are transactions. A recurring schedule is a template; the firings are transactions.
3. **Balances are computed.** No materialised balance column. `SELECT SUM(...)` on demand.

## Conventions

- IDs are UUIDv7 stored as `BLOB(16)`. Time-ordered so inserts stay sequential in the B-tree and `ORDER BY id` is a rough chronological order.
- Money is `INTEGER` minor units (paise for PKR). No floats anywhere.
- Timestamps are epoch milliseconds as `INTEGER`; `kotlinx.datetime.Instant` in Kotlin.
- `PRAGMA foreign_keys = ON` and `journal_mode = WAL` set by the driver on every connection. Both are per-connection in SQLite — not persistent.
- Every table has `created_at` and `updated_at`.

---

## accounts

Holds both real-money locations (Bank, Cash) and budget envelopes (Food, Transport). A budget points at the real account it allocates from via `parent_account_id`.

| column                  | type          | notes                            |
|-------------------------|---------------|----------------------------------|
| id                      | BLOB PK       | UUIDv7                           |
| name                    | TEXT          | unique among non-archived        |
| type                    | TEXT          | `REAL` \| `BUDGET`               |
| currency                | TEXT          | ISO 4217, `PKR` at launch        |
| parent_account_id       | BLOB FK NULL  | NULL for REAL, required for BUDGET |
| opening_balance_minor   | INTEGER       | default 0                        |
| color                   | TEXT NULL     | UI                               |
| icon                    | TEXT NULL     | UI                               |
| display_order           | INTEGER       | default 0                        |
| created_at              | INTEGER       |                                  |
| updated_at              | INTEGER       |                                  |
| archived_at             | INTEGER NULL  | hides from default views         |

**Constraints**

- `CHECK ((type='REAL' AND parent_account_id IS NULL) OR (type='BUDGET' AND parent_account_id IS NOT NULL))`
- Trigger: a BUDGET's parent must be `type='REAL'` (CHECK can't subquery).
- Trigger: a BUDGET's `currency` must equal its parent's `currency`.
- Partial unique index on `name` where `archived_at IS NULL`.

**Why one table for both kinds?** Real and budget accounts have identical mechanics — both can be the source or destination of a transaction. Splitting them would force a polymorphic `from`/`to` or duplicate ledger logic. One table with a `type` discriminator keeps reads simple.

**Why archive instead of delete?** Hard-deleting an account would orphan transactions or force a cascade that erases history. Archive hides without losing the past.

**Balance formula.** `opening_balance_minor + Σ(amount where to_account_id = id) − Σ(amount where from_account_id = id)`.

---

## transactions

The ledger. One row per cash event.

| column            | type          | notes                                |
|-------------------|---------------|--------------------------------------|
| id                | BLOB PK       | UUIDv7                               |
| kind              | TEXT          | see kinds below                      |
| from_account_id   | BLOB FK NULL  | source                               |
| to_account_id     | BLOB FK NULL  | destination                          |
| amount_minor      | INTEGER       | `> 0`                                |
| category_id       | BLOB FK NULL  | `ON DELETE SET NULL`                 |
| note              | TEXT NULL     |                                      |
| recorded_at       | INTEGER       | user-facing date                     |
| recurring_id      | BLOB FK NULL  | `ON DELETE SET NULL`                 |
| debt_id           | BLOB FK NULL  | `DEFERRABLE INITIALLY DEFERRED`      |
| created_at        | INTEGER       |                                      |
| updated_at        | INTEGER       |                                      |

**Kinds** (stored, not derived)

| kind             | from   | to     | category | debt | recurring |
|------------------|--------|--------|----------|------|-----------|
| `INCOME`         | NULL   | REAL   | required | NULL | optional  |
| `EXPENSE`        | any    | NULL   | required | NULL | optional  |
| `TRANSFER`       | REAL   | REAL   | NULL     | NULL | NULL      |
| `ALLOCATION`     | REAL   | BUDGET | NULL     | NULL | NULL      |
| `REALLOCATION`   | BUDGET | BUDGET | NULL     | NULL | NULL      |
| `DEBT_BORROW`    | NULL   | REAL   | NULL     | req  | NULL      |
| `DEBT_LEND`      | REAL   | NULL   | NULL     | req  | NULL      |
| `DEBT_REPAY_OUT` | REAL   | NULL   | NULL     | req  | NULL      |
| `DEBT_REPAY_IN`  | NULL   | REAL   | NULL     | req  | NULL      |

Nullability per kind is enforced by `CHECK`. Account-type constraints (e.g., TRANSFER endpoints must be REAL) are domain invariants — the UI only offers valid pickers.

**Constraints**

- `CHECK (amount_minor > 0)`.
- Per-kind shape `CHECK` (see Kinds table).
- `CHECK (NOT (recurring_id IS NOT NULL AND debt_id IS NOT NULL))` — recurring schedules don't fire debt payments in v0.
- `CHECK ((kind IN ('INCOME','EXPENSE')) OR recurring_id IS NULL)` — recurring is only allowed on income/expense kinds.
- `CHECK (from_account_id IS NULL OR to_account_id IS NULL OR from_account_id != to_account_id)` — no self-loops.

**Cross-row triggers** (close the gaps `CHECK` can't cover):

- Currency match: both endpoints of a multi-account row must share `currency`.
- Category scope: `INCOME` rows take INCOME-scope categories; `EXPENSE` rows take EXPENSE-scope.
- Reallocation parent: both budgets in a `REALLOCATION` must share `parent_account_id`.
- Archived accounts: a new row's `recorded_at` may not be after either endpoint's `archived_at`.

**Why store `kind`?** Without it the schema accepts ghost rows (all FKs NULL) and every consumer has to re-derive the type from a five-column nullability pattern plus an account-type join. Storing `kind` makes the DB the arbiter and trivialises every read query.

**Why no `SPEND_FROM_BUDGET` kind?** It's just an `EXPENSE` whose `from` is a BUDGET. The budget vs real distinction is recoverable from `accounts.type` when reports need it.

**Why the deferred FK on `debt_id`?** `debts.origin_transaction_id` references back to `transactions.id`, creating a cycle. Deferred FKs let both inserts sit inside one transaction and validate at COMMIT — no `UPDATE` step.

---

## categories

Tags for expense and income. Defaults are seeded once and immutable; customs are user-created.

| column        | type         | notes                              |
|---------------|--------------|------------------------------------|
| id            | BLOB PK      |                                    |
| name          | TEXT         |                                    |
| scope         | TEXT         | `EXPENSE` \| `INCOME`              |
| is_default    | INTEGER      | `0` \| `1`; immutable when `1`     |
| icon          | TEXT NULL    |                                    |
| color         | TEXT NULL    |                                    |
| display_order | INTEGER      | default 0                          |
| created_at    | INTEGER      |                                    |
| updated_at    | INTEGER      |                                    |
| archived_at   | INTEGER NULL | only valid when `is_default = 0`   |

**Why archive on customs?** A used category can't be deleted (FK `SET NULL` would erase the tag from history). Archive hides it from the picker without disturbing past rows. Defaults can't be archived — they're part of the shipped vocabulary.

**Why no "override default" semantics?** Editing seeded data forks the shipped set and complicates future migrations. To get "Food" instead of "Food & Dining", the user creates a custom — boring, predictable.

---

## persons

Contacts for debts.

| column            | type         | notes                                          |
|-------------------|--------------|------------------------------------------------|
| id                | BLOB PK      |                                                |
| name              | TEXT         | unique among non-archived, case-insensitive    |
| avatar_tint_index | INTEGER      | 1–5                                            |
| created_at        | INTEGER      |                                                |
| updated_at        | INTEGER      |                                                |
| archived_at       | INTEGER NULL |                                                |

Partial unique index: `(name COLLATE NOCASE) WHERE archived_at IS NULL`.

**Why case-insensitive uniqueness?** Prevents "Ahmed" and "ahmed" from becoming two contacts via picker typos.

---

## recurring_schedules

Templates. Firings are transactions with `recurring_id` set.

| column          | type         | notes                                 |
|-----------------|--------------|---------------------------------------|
| id              | BLOB PK      |                                       |
| name            | TEXT         |                                       |
| amount_minor    | INTEGER      |                                       |
| from_account_id | BLOB FK NULL | stamped onto each firing              |
| to_account_id   | BLOB FK NULL | stamped onto each firing              |
| category_id     | BLOB FK NULL | stamped onto each firing              |
| cadence         | TEXT         | `WEEKLY` \| `MONTHLY` \| `YEARLY`     |
| day_of_week     | INTEGER NULL | 1–7 for WEEKLY                        |
| day_of_month    | INTEGER NULL | 1–31 for MONTHLY, YEARLY              |
| month_of_year   | INTEGER NULL | 1–12 for YEARLY                       |
| next_due_at     | INTEGER      | epoch ms                              |
| status          | TEXT         | `ACTIVE` \| `PAUSED`                  |
| paused_at       | INTEGER NULL |                                       |
| created_at      | INTEGER      |                                       |
| updated_at      | INTEGER      |                                       |

**Constraints**

- `CHECK` that the right cadence field is populated and the others are NULL.
- `day_of_month` accepts 1–31; the next-due-at calculator clamps Feb 31 to end-of-month. The user's "due on the 31st" intent is preserved.

**Why split day fields per cadence?** A single `day` integer would encode three meanings into one column — bad for filters and unclear in queries. Separate fields are self-describing and survive future cadences (biweekly, quarterly) without a migration.

**Why no instances table?** Pending is computed (`next_due_at ≤ now AND status = ACTIVE`); history is `WHERE recurring_id = ?`; a skipped firing is just an advancement of `next_due_at`. Nothing in the UI surfaces "you skipped Netflix in April."

---

## debts

The relationship. Money movement (origin, repayments) lives in `transactions` with `debt_id` set.

| column                | type         | notes                                  |
|-----------------------|--------------|----------------------------------------|
| id                    | BLOB PK      |                                        |
| person_id             | BLOB FK      | RESTRICT                               |
| direction             | TEXT         | `I_OWE` \| `OWED_TO_ME`                |
| original_amount_minor | INTEGER      |                                        |
| currency              | TEXT         | matches the origin transaction's account |
| reason                | TEXT NULL    |                                        |
| status                | TEXT         | `ACTIVE` \| `SETTLED` (user-set)       |
| origin_transaction_id | BLOB FK      | the first money move; `DEFERRABLE`     |
| created_at            | INTEGER      |                                        |
| updated_at            | INTEGER      |                                        |
| settled_at            | INTEGER NULL |                                        |

**Remaining amount** is computed:

```
direction = I_OWE:        original − Σ(DEBT_REPAY_OUT amounts)
direction = OWED_TO_ME:   original − Σ(DEBT_REPAY_IN amounts)
```

**Why is `status` user-set, not computed?** Forgiveness, write-offs, and "we called it even" are real flows. Computing `SETTLED ↔ remaining = 0` would block them.

**Why `currency` on the debt?** When multi-currency arrives later, the debt's currency is the anchor for valid repayment currencies. Adding the column later means backfilling from origin transactions; adding now costs nothing.

**Why pin `origin_transaction_id`?** The borrow/lend transaction is labelled differently in the timeline ("Borrowed from Ahmed"). The pin makes that a one-row lookup instead of a heuristic.

A `BEFORE INSERT` trigger enforces that `currency` matches the origin transaction's account currency.

---

## Cross-cutting

**FK delete policies.** Default RESTRICT, with two exceptions:

| FK                              | policy        | reason                        |
|---------------------------------|---------------|-------------------------------|
| `transactions.category_id`      | `SET NULL`    | Deleting a custom category shouldn't block deleting historical rows — the row just loses its tag. |
| `transactions.recurring_id`     | `SET NULL`    | Deleting a schedule shouldn't block the user; history retains the row, loses the back-pointer. |
| `transactions.from_account_id`  | `RESTRICT`    | Archive the account instead.  |
| `transactions.to_account_id`    | `RESTRICT`    | Same.                         |
| `transactions.debt_id`          | `RESTRICT`    | Delete via the debt screen.   |
| `debts.person_id`               | `RESTRICT`    | Person with active debt can't be deleted. |
| `debts.origin_transaction_id`   | `RESTRICT`    | Structural.                   |
| `recurring_schedules.*`         | `RESTRICT`    | Template needs valid pointers.|

**Deferred FKs** on the `transactions ↔ debts` cycle:

```sql
transactions.debt_id              DEFERRABLE INITIALLY DEFERRED
debts.origin_transaction_id       DEFERRABLE INITIALLY DEFERRED
```

Two inserts in one `BEGIN…COMMIT`; FK validated at commit.

**Schema versioning.** A small `schema_meta(key, value)` table holds at least `version` and a per-install `device_id`. Useful for future sync; trivial to add now, painful to retrofit later.

---

## Indices

```sql
CREATE INDEX tx_recorded_at        ON transactions(recorded_at DESC);
CREATE INDEX tx_from_recorded      ON transactions(from_account_id, recorded_at DESC);
CREATE INDEX tx_to_recorded        ON transactions(to_account_id,   recorded_at DESC);
CREATE INDEX tx_category_recorded  ON transactions(category_id,     recorded_at DESC)
                                   WHERE category_id  IS NOT NULL;
CREATE INDEX tx_debt               ON transactions(debt_id)         WHERE debt_id      IS NOT NULL;
CREATE INDEX tx_recurring          ON transactions(recurring_id)    WHERE recurring_id IS NOT NULL;
CREATE INDEX tx_kind_recorded      ON transactions(kind, recorded_at DESC);
CREATE INDEX recurring_due         ON recurring_schedules(status, next_due_at);
CREATE INDEX debts_status_person   ON debts(status, person_id);
```

---

## Worked rows

| operation                  | kind             | from   | to     | category | debt | recurring |
|----------------------------|------------------|--------|--------|----------|------|-----------|
| Salary into Bank           | `INCOME`         | NULL   | Bank   | Salary   | NULL | NULL      |
| Allocate Bank → Food       | `ALLOCATION`     | Bank   | Food   | NULL     | NULL | NULL      |
| Move Fun → Food            | `REALLOCATION`   | Fun    | Food   | NULL     | NULL | NULL      |
| Lunch from Food            | `EXPENSE`        | Food   | NULL   | Food     | NULL | NULL      |
| Rent from Bank             | `EXPENSE`        | Bank   | NULL   | Rent     | NULL | NULL      |
| Bank → Savings             | `TRANSFER`       | Bank   | Saving | NULL     | NULL | NULL      |
| Borrow 8,000 from Ahmed    | `DEBT_BORROW`    | NULL   | Bank   | NULL     | 42   | NULL      |
| Repay Ahmed 2,000          | `DEBT_REPAY_OUT` | Bank   | NULL   | NULL     | 42   | NULL      |
| Lend 5,000 to Sara         | `DEBT_LEND`      | Bank   | NULL   | NULL     | 51   | NULL      |
| Receive 3,000 from Sara    | `DEBT_REPAY_IN`  | NULL   | Bank   | NULL     | 51   | NULL      |
| Netflix recurring fires    | `EXPENSE`        | Bank   | NULL   | Subs     | NULL | 7         |

Borrow and lend are two-row inserts inside one deferred-FK transaction: the transaction, plus the `debts` row pointing back via `origin_transaction_id`.

---

## Decisions

- **Single-row transactions, not double-entry.** We don't need split transactions or an audit ledger; the simpler shape is enough.
- **`kind` stored, not derived.** DB-enforced row shapes beat re-deriving at every read.
- **Computed balances, not materialised.** `SELECT SUM(...)` is fast at this volume; eliminates a class of consistency bugs.
- **No instances table for recurring.** Skipped firings aren't surfaced.
- **UUIDv7 `BLOB(16)` ids.** Cheap to retrofit sync later; 16 bytes vs 36 for TEXT UUIDs.
- **Allocation has no category.** Budgets are envelopes; categories tag real spend. Allocation reads as a transfer between accounts.
- **Attachments deferred.** Additive `attachments` table when needed.
- **Hard delete with confirm**, except `archived_at` on `accounts`, `categories` (customs only), and `persons` — entities whose pickers would otherwise rot.
- **`status` on debts is user-set**, allowing forgiveness.
- **Settings live in Proto DataStore**, not here.
