package com.flowfin.devtools

import com.flowfin.core.database.FlowFinDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Debug-only: empties every table in one transaction, in foreign-key-safe order.
 * Children before parents — transactions reference accounts (RESTRICT), budgets
 * reference their real parent, debts reference persons — so the order below never
 * trips a constraint without disabling enforcement.
 */
internal class DevReset(
  private val db: FlowFinDatabase,
  private val dispatcher: CoroutineDispatcher,
) {
  suspend fun wipeAll() = withContext(dispatcher) {
    db.devQueries.transaction {
      db.devQueries.deleteAllTransactions()
      db.devQueries.deleteAllRecurring()
      db.devQueries.deleteAllDebts()
      db.devQueries.deleteBudgets() // budgets reference a real parent
      db.devQueries.deleteAllAccounts()
      db.devQueries.deleteAllCategories()
      db.devQueries.deleteAllPersons()
    }
  }
}
