package com.flowfin.core.data

import com.flowfin.core.database.Accounts
import com.flowfin.core.database.BalanceForAll
import com.flowfin.core.database.Categories
import com.flowfin.core.database.Debts
import com.flowfin.core.database.Persons
import com.flowfin.core.database.SelectAllWithRemaining
import com.flowfin.core.database.SelectByDirectionWithRemaining
import com.flowfin.core.database.Transactions
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountBalance
import com.flowfin.core.model.Category
import com.flowfin.core.model.Debt
import com.flowfin.core.model.DebtWithRemaining
import com.flowfin.core.model.Money
import com.flowfin.core.model.Person
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

internal fun Categories.toModel(): Category = Category(
  id = id,
  name = name,
  scope = scope,
  isDefault = is_default != 0L,
  icon = icon,
  color = color,
  displayOrder = display_order.toInt(),
  createdAt = created_at,
  updatedAt = updated_at,
  archivedAt = archived_at,
)

internal fun Persons.toModel(): Person = Person(
  id = id,
  name = name,
  avatarTintIndex = avatar_tint_index.toInt(),
  createdAt = created_at,
  updatedAt = updated_at,
  archivedAt = archived_at,
)

internal fun Debts.toModel(): Debt = Debt(
  id = id,
  personId = person_id,
  direction = direction,
  originalAmount = Money(original_amount_minor),
  currency = currency,
  reason = reason,
  status = status,
  originTransactionId = origin_transaction_id,
  createdAt = created_at,
  updatedAt = updated_at,
  settledAt = settled_at,
)

internal fun SelectAllWithRemaining.toDebtWithRemaining(): DebtWithRemaining = DebtWithRemaining(
  debt = Debt(
    id = id,
    personId = person_id,
    direction = direction,
    originalAmount = Money(original_amount_minor),
    currency = currency,
    reason = reason,
    status = status,
    originTransactionId = origin_transaction_id,
    createdAt = created_at,
    updatedAt = updated_at,
    settledAt = settled_at,
  ),
  paid = Money(paid_minor),
  remaining = Money(remaining_minor),
)

internal fun SelectByDirectionWithRemaining.toDebtWithRemaining(): DebtWithRemaining = DebtWithRemaining(
  debt = Debt(
    id = id,
    personId = person_id,
    direction = direction,
    originalAmount = Money(original_amount_minor),
    currency = currency,
    reason = reason,
    status = status,
    originTransactionId = origin_transaction_id,
    createdAt = created_at,
    updatedAt = updated_at,
    settledAt = settled_at,
  ),
  paid = Money(paid_minor),
  remaining = Money(remaining_minor),
)
