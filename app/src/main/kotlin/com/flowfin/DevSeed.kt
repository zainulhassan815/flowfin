package com.flowfin

import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.Money
import com.flowfin.core.model.TransactionDraft
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Temporary scaffolding: seeds a little sample data the first time the app runs
 * so the wired Home screen has something to show before any create-flows exist.
 * Remove once accounts and transactions can be created from the UI. Assumes the
 * default categories have already been seeded.
 */
suspend fun seedSampleDataIfEmpty(
  accounts: AccountRepository,
  categories: CategoryRepository,
  createRealAccount: CreateRealAccount,
  createBudget: CreateBudget,
  recordTransaction: RecordTransaction,
  clock: Clock,
) {
  if (accounts.observeActiveAccounts().first().isNotEmpty()) return

  val now = clock.now()
  val bank = createRealAccount("Bank", openingBalance = Money(5_000_000)).getOrNull() ?: return
  val cash = createRealAccount("Cash", openingBalance = Money(500_000)).getOrNull() ?: return
  val food = createBudget("Food", bank.id).getOrNull() ?: return

  recordTransaction(TransactionDraft.Allocation(bank.id, food.id, Money(1_000_000), recordedAt = now))
  recordTransaction(TransactionDraft.Transfer(bank.id, cash.id, Money(200_000), note = null, recordedAt = now))

  val salary = categories.observeByScope(CategoryScope.INCOME).first().firstOrNull { it.name == "Salary" }
  val groceries = categories.observeByScope(CategoryScope.EXPENSE).first().firstOrNull { it.name == "Groceries" }
  if (salary != null) {
    recordTransaction(TransactionDraft.Income(bank.id, Money(15_000_000), salary.id, note = null, recordedAt = now))
  }
  if (groceries != null) {
    recordTransaction(TransactionDraft.Expense(bank.id, Money(500_000), groceries.id, note = null, recordedAt = now))
  }
}
