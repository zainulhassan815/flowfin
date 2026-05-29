package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.domain.error.AccountError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountType
import com.flowfin.core.model.Money

/** Creates a real-money account (Bank, Cash). PKR at launch. */
class CreateRealAccount(
  private val accounts: AccountRepository,
) {
  suspend operator fun invoke(
    name: String,
    currency: String = "PKR",
    openingBalance: Money = Money.ZERO,
    color: String? = null,
    icon: String? = null,
    displayOrder: Int = 0,
  ): Either<AccountError, Account> = either {
    ensure(name.isNotBlank()) { AccountError.NameBlank }
    accounts.create(
      name = name.trim(),
      type = AccountType.REAL,
      currency = currency,
      parentAccountId = null,
      openingBalance = openingBalance,
      color = color,
      icon = icon,
      displayOrder = displayOrder,
    ).bind()
  }
}
