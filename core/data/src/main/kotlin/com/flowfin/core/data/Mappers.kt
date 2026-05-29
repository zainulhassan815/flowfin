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
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.DebtStatus
import com.flowfin.core.model.DebtWithRemaining
import com.flowfin.core.model.Money
import com.flowfin.core.model.Person
import com.flowfin.core.model.PersonId
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionId
import kotlinx.datetime.Instant

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

// The two `*WithRemaining` queries are distinct generated types with identical
// columns, so both map through one builder.
internal fun SelectAllWithRemaining.toDebtWithRemaining(): DebtWithRemaining =
  debtWithRemaining(
    id, person_id, direction, original_amount_minor, currency, reason, status,
    origin_transaction_id, created_at, updated_at, settled_at, paid_minor, remaining_minor,
  )

internal fun SelectByDirectionWithRemaining.toDebtWithRemaining(): DebtWithRemaining =
  debtWithRemaining(
    id, person_id, direction, original_amount_minor, currency, reason, status,
    origin_transaction_id, created_at, updated_at, settled_at, paid_minor, remaining_minor,
  )

private fun debtWithRemaining(
  id: DebtId,
  personId: PersonId,
  direction: DebtDirection,
  originalAmountMinor: Long,
  currency: String,
  reason: String?,
  status: DebtStatus,
  originTransactionId: TransactionId,
  createdAt: Instant,
  updatedAt: Instant,
  settledAt: Instant?,
  paidMinor: Long,
  remainingMinor: Long,
): DebtWithRemaining = DebtWithRemaining(
  debt = Debt(
    id = id,
    personId = personId,
    direction = direction,
    originalAmount = Money(originalAmountMinor),
    currency = currency,
    reason = reason,
    status = status,
    originTransactionId = originTransactionId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    settledAt = settledAt,
  ),
  paid = Money(paidMinor),
  remaining = Money(remainingMinor),
)
