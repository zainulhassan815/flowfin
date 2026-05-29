package com.flowfin

import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.domain.usecase.RecordTransaction
import com.flowfin.core.model.Money
import com.flowfin.core.model.TransactionDraft
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Temporary scaffolding: seeds a little sample data the first time the app runs
 * so the wired Home screen has something to show before any create-flows exist.
 * Remove once accounts and transactions can be created from the UI.
 */
suspend fun seedSampleDataIfEmpty(
  accounts: AccountRepository,
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
}
