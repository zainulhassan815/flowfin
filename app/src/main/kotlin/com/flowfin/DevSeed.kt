package com.flowfin

import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.usecase.CreateBudget
import com.flowfin.core.domain.usecase.CreateRealAccount
import com.flowfin.core.model.Money
import kotlinx.coroutines.flow.first

/**
 * Temporary scaffolding: seeds a couple of accounts the first time the app runs so
 * there's something to spend from before an Add-Account flow exists. Transactions
 * are now added through the UI (Add Transaction), so none are seeded. Remove once
 * accounts can be created from the UI too.
 */
suspend fun seedSampleAccountsIfEmpty(
  accounts: AccountRepository,
  createRealAccount: CreateRealAccount,
  createBudget: CreateBudget,
) {
  if (accounts.observeActiveAccounts().first().isNotEmpty()) return

  val bank = createRealAccount("Bank", openingBalance = Money(5_000_000)).getOrNull() ?: return
  createRealAccount("Cash", openingBalance = Money(500_000))
  createBudget("Food", bank.id)
}
