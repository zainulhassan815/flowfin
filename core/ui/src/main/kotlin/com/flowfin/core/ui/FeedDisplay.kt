package com.flowfin.core.ui

import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.model.Account
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.resources.R
import kotlinx.datetime.LocalDate

/**
 * Display mapping shared by every transaction feed (Home, Account detail). A row
 * is joined in-memory against the supplied account and category lookups; the +/−
 * sign and the 3-way colour bucket ([RowKind]) follow from the domain
 * [TransactionKind]. Money is already formatted by [money]; the screen resolves
 * the [UiText] name and the icon/colour keys.
 */
fun Transaction.toRowUi(
  accountsById: Map<AccountId, Account>,
  categoriesById: Map<CategoryId, Category>,
  money: MoneyFormatter,
  perspective: AccountId? = null,
): TxRowUi {
  val category = categoryId?.let { categoriesById[it] }
  val fromName = fromAccountId?.let { accountsById[it]?.name }.orEmpty()
  val toName = toAccountId?.let { accountsById[it]?.name }.orEmpty()
  // Money enters an account iff it's the destination. Viewed from one account
  // ([perspective]) this flips an internal move's sign — a transfer reads +in on the
  // receiver and −out on the payer; the global feed keeps its by-kind convention.
  val moneyIn = if (perspective != null) toAccountId == perspective else kind.rowKind() == RowKind.Income
  return TxRowUi(
    id = id,
    name = when (kind) {
      TransactionKind.INCOME, TransactionKind.EXPENSE ->
        category?.name?.let(UiText::Raw) ?: UiText.Res(R.string.tx_uncategorized)
      TransactionKind.TRANSFER -> UiText.Res(R.string.tx_transfer)
      TransactionKind.ALLOCATION -> UiText.Res(R.string.tx_allocation)
      TransactionKind.REALLOCATION -> UiText.Res(R.string.tx_reallocation)
      TransactionKind.DEBT_BORROW, TransactionKind.DEBT_LEND,
      TransactionKind.DEBT_REPAY_OUT, TransactionKind.DEBT_REPAY_IN -> UiText.Res(R.string.tx_debt)
    },
    meta = rowMeta(perspective, fromName, toName),
    amount = (if (moneyIn) "+" else "−") + money.whole(amount),
    decimal = money.fraction(amount),
    kind = kind.rowKind(),
    iconKey = when (kind) {
      TransactionKind.INCOME, TransactionKind.EXPENSE -> category?.icon
      else -> "sync_alt"
    },
    colorKey = when (kind) {
      TransactionKind.INCOME, TransactionKind.EXPENSE -> category?.color
      else -> null
    },
  )
}

/**
 * A day's heading in a transaction feed — "Today · 27 Dec", "Mon · 22 Dec".
 * [today] is sampled once by the caller so "Today" / "Yesterday" stay stable for
 * the screen's life. Weekday/month names are still English enum short forms;
 * full date localization is a separate effort.
 */
fun dateLabel(date: LocalDate, today: LocalDate): UiText {
  val prefix = when (date.toEpochDays()) {
    today.toEpochDays() -> UiText.Res(R.string.date_today)
    today.toEpochDays() - 1 -> UiText.Res(R.string.date_yesterday)
    else -> UiText.Raw(date.dayOfWeek.name.titleCase3())
  }
  return UiText.Res(R.string.date_label, listOf(prefix, date.dayOfMonth, date.month.name.titleCase3()))
}

/**
 * The 3-way display bucket for a domain [TransactionKind] — the single source of
 * truth shared by a row's colour/sign (here), the ledger's per-month net, and the
 * ledger's filter. Income covers external inflows (income, borrowing, repayments
 * received); Expense the outflows; Transfer the internal moves (transfer,
 * allocation, reallocation).
 */
fun TransactionKind.rowKind(): RowKind = when (this) {
  TransactionKind.INCOME, TransactionKind.DEBT_BORROW, TransactionKind.DEBT_REPAY_IN -> RowKind.Income
  TransactionKind.EXPENSE, TransactionKind.DEBT_LEND, TransactionKind.DEBT_REPAY_OUT -> RowKind.Expense
  TransactionKind.TRANSFER, TransactionKind.ALLOCATION, TransactionKind.REALLOCATION -> RowKind.Transfer
}

/**
 * A row's secondary line. The global feed names the account(s); from one account's
 * view ([perspective]) an internal move names the counterparty, and income/expense
 * surface the note (the title already names the category).
 */
private fun Transaction.rowMeta(perspective: AccountId?, fromName: String, toName: String): String = when {
  perspective == null -> when (kind) {
    TransactionKind.INCOME -> toName
    TransactionKind.EXPENSE -> fromName
    TransactionKind.TRANSFER, TransactionKind.ALLOCATION, TransactionKind.REALLOCATION -> "$fromName → $toName"
    else -> ""
  }
  kind == TransactionKind.TRANSFER || kind == TransactionKind.ALLOCATION || kind == TransactionKind.REALLOCATION ->
    if (toAccountId == perspective) "From $fromName" else "To $toName"
  else -> note.orEmpty()
}

/** A month's short label — "Dec". Shared by feeds and the Account-detail flow strip. */
fun monthShortLabel(date: LocalDate): String = date.month.name.titleCase3()

/** "MONDAY" → "Mon", "DECEMBER" → "Dec". */
private fun String.titleCase3(): String =
  take(3).lowercase().replaceFirstChar { it.uppercase() }
