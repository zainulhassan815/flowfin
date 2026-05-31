package com.flowfin.core.ui

import com.flowfin.core.designsystem.component.BudgetProgress
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.AccountId

/**
 * Display mapping for accounts, shared by every surface that shows one (Home,
 * Accounts list, Account detail). The icon/colour key defaults and the budget's
 * parent-name lookup live here so all three stay in lockstep — features pass the
 * keys to `categoryIcon` / `categoryColor`.
 */

/** Stored icon key, or the type's default (budgets read as an envelope/wallet). */
fun Account.iconKey(): String = icon ?: if (isBudget) "wallet" else "bank"

/** Stored colour key, or the type's default. */
fun Account.colorKey(): String = color ?: if (isBudget) "other" else "bank"

/** A budget's parent-account name (for the card's meta line); empty for real accounts. */
fun Account.parentName(accountsById: Map<AccountId, Account>): String =
  if (isBudget) parentAccountId?.let { accountsById[it]?.name }.orEmpty() else ""

/**
 * Maps an [AccountBalance] to its display card. [progress] is set only for budget
 * envelopes shown as boxed cards (spend vs. funded) and defaults to none.
 */
fun AccountBalance.toCardUi(
  accountsById: Map<AccountId, Account>,
  money: MoneyFormatter,
  progress: BudgetProgress? = null,
): AccountCardUi = AccountCardUi(
  id = account.id,
  name = account.name,
  meta = account.parentName(accountsById),
  balanceWhole = money.whole(balance),
  balanceDecimal = money.fraction(balance),
  iconKey = account.iconKey(),
  colorKey = account.colorKey(),
  isBudget = account.isBudget,
  progress = progress,
)
