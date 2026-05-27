package com.flowfin.core.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Builds the SQLite driver backing [FlowFinDatabase].
 *
 * Sets `PRAGMA foreign_keys = ON` and `journal_mode = WAL` on every
 * connection — both pragmas are per-connection in SQLite and don't persist,
 * so they must be re-applied here.
 */
fun flowFinDatabaseDriver(
  context: Context,
  fileName: String = "flowfin.db",
): SqlDriver = AndroidSqliteDriver(
  schema = FlowFinDatabase.Schema,
  context = context,
  name = fileName,
  callback = object : AndroidSqliteDriver.Callback(FlowFinDatabase.Schema) {
    override fun onOpen(db: SupportSQLiteDatabase) {
      super.onOpen(db)
      db.setForeignKeyConstraintsEnabled(true)
      db.execSQL("PRAGMA journal_mode = WAL")
      db.execSQL("PRAGMA synchronous = NORMAL")
    }
  },
)

/** Constructs the typed [FlowFinDatabase] with every column adapter wired up. */
fun flowFinDatabase(driver: SqlDriver): FlowFinDatabase = FlowFinDatabase(
  driver = driver,
  accountsAdapter = Accounts.Adapter(
    idAdapter = AccountIdAdapter,
    typeAdapter = AccountTypeAdapter,
    parent_account_idAdapter = AccountIdAdapter,
    created_atAdapter = InstantAdapter,
    updated_atAdapter = InstantAdapter,
    archived_atAdapter = InstantAdapter,
  ),
  categoriesAdapter = Categories.Adapter(
    idAdapter = CategoryIdAdapter,
    scopeAdapter = CategoryScopeAdapter,
    created_atAdapter = InstantAdapter,
    updated_atAdapter = InstantAdapter,
    archived_atAdapter = InstantAdapter,
  ),
  personsAdapter = Persons.Adapter(
    idAdapter = PersonIdAdapter,
    created_atAdapter = InstantAdapter,
    updated_atAdapter = InstantAdapter,
    archived_atAdapter = InstantAdapter,
  ),
  recurring_schedulesAdapter = Recurring_schedules.Adapter(
    idAdapter = RecurringScheduleIdAdapter,
    from_account_idAdapter = AccountIdAdapter,
    to_account_idAdapter = AccountIdAdapter,
    category_idAdapter = CategoryIdAdapter,
    cadenceAdapter = CadenceAdapter,
    next_due_atAdapter = InstantAdapter,
    statusAdapter = RecurringStatusAdapter,
    paused_atAdapter = InstantAdapter,
    created_atAdapter = InstantAdapter,
    updated_atAdapter = InstantAdapter,
  ),
  debtsAdapter = Debts.Adapter(
    idAdapter = DebtIdAdapter,
    person_idAdapter = PersonIdAdapter,
    directionAdapter = DebtDirectionAdapter,
    statusAdapter = DebtStatusAdapter,
    origin_transaction_idAdapter = TransactionIdAdapter,
    created_atAdapter = InstantAdapter,
    updated_atAdapter = InstantAdapter,
    settled_atAdapter = InstantAdapter,
  ),
  transactionsAdapter = Transactions.Adapter(
    idAdapter = TransactionIdAdapter,
    kindAdapter = TransactionKindAdapter,
    from_account_idAdapter = AccountIdAdapter,
    to_account_idAdapter = AccountIdAdapter,
    category_idAdapter = CategoryIdAdapter,
    recorded_atAdapter = InstantAdapter,
    recurring_idAdapter = RecurringScheduleIdAdapter,
    debt_idAdapter = DebtIdAdapter,
    created_atAdapter = InstantAdapter,
    updated_atAdapter = InstantAdapter,
  ),
)
