package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Money

/**
 * Creates a budget envelope under a real parent account. The budget inherits the
 * parent's currency, so the caller doesn't supply one. Both the parent-is-REAL
 * and currency-match rules are also database triggers; checked here for a typed
 * error instead of a raw abort.
 */
class CreateBudget(
  private val accounts: AccountRepository,
) {
  suspend operator fun invoke(
    name: String,
    parentAccountId: AccountId,
    openingBalance: Money = Money.ZERO,
    color: String? = null,
    icon: String? = null,
    displayOrder: Int = 0,
  ): Either<AccountError, Account> = either {
    ensure(name.isNotBlank()) { AccountError.NameBlank }
    val parent = accounts.getById(parentAccountId) ?: raise(AccountError.ParentNotFound(parentAccountId))
    ensure(parent.isReal) { AccountError.ParentNotReal }
    accounts.create(
      name = name.trim(),
      type = AccountType.BUDGET,
      currency = parent.currency,
      parentAccountId = parentAccountId,
      openingBalance = openingBalance,
      color = color,
      icon = icon,
      displayOrder = displayOrder,
    ).bind()
  }
}
