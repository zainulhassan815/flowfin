package com.flowfin.core.data

import com.flowfin.core.database.Accounts
import com.flowfin.core.database.BalanceForAll
import com.flowfin.core.database.Transactions
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.Money
import com.flowfin.core.model.Transaction

/**
 * SQLDelight row → domain model. The database stays in primitive `Long` minor
 * units; the `Money` wrapper is applied here, at the data edge.
 */

internal fun Accounts.toModel(): Account = Account(
  id = id,
  name = name,
  type = type,
  currency = currency,
  parentAccountId = parent_account_id,
  openingBalance = Money(opening_balance_minor),
  color = color,
  icon = icon,
  displayOrder = display_order.toInt(),
  createdAt = created_at,
  updatedAt = updated_at,
  archivedAt = archived_at,
)

internal fun BalanceForAll.toAccountBalance(): AccountBalance = AccountBalance(
  account = Account(
    id = id,
    name = name,
    type = type,
    currency = currency,
    parentAccountId = parent_account_id,
    openingBalance = Money(opening_balance_minor),
    color = color,
    icon = icon,
    displayOrder = display_order.toInt(),
    createdAt = created_at,
    updatedAt = updated_at,
    archivedAt = archived_at,
  ),
  balance = Money(balance_minor),
)

internal fun Transactions.toModel(): Transaction = Transaction(
  id = id,
  kind = kind,
  fromAccountId = from_account_id,
  toAccountId = to_account_id,
  amount = Money(amount_minor),
  categoryId = category_id,
  note = note,
  recordedAt = recorded_at,
  recurringId = recurring_id,
  debtId = debt_id,
  createdAt = created_at,
  updatedAt = updated_at,
)
