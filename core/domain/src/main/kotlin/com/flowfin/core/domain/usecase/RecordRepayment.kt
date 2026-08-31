package com.flowfin.core.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.flowfin.core.domain.error.DebtError
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.DebtId
import com.flowfin.core.model.Money
import kotlinx.datetime.Instant

/**
 * Records a repayment against a debt. Overpayment is allowed (settling up or
 * forgiveness), so there's no check against the remaining; the direction of the
 * debt decides whether money leaves or enters the account. [account] is
 * optional — an off-book debt (or an off-book repayment on a linked one) has no
 * account to move money through, and so nothing to currency-check either.
 */
class RecordRepayment(
  private val accounts: AccountRepository,
  private val debts: DebtRepository,
) {
  suspend operator fun invoke(
    debtId: DebtId,
    account: AccountId?,
    amount: Money,
    recordedAt: Instant,
    note: String? = null,
  ): Either<DebtError, Unit> = either {
    ensure(amount.isPositive) { DebtError.AmountNotPositive }
    val debt = debts.getById(debtId) ?: raise(DebtError.DebtNotFound(debtId))
    if (account != null) {
      val real = requireRealActiveAccount(accounts, account)
      ensure(real.currency == debt.currency) { DebtError.CurrencyMismatch }
    }
    debts.recordRepayment(debt, account, amount, note, recordedAt).bind()
  }
}
