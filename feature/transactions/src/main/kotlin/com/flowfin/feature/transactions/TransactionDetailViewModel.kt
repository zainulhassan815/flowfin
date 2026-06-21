package com.flowfin.feature.transactions

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.Category
import com.flowfin.core.model.Transaction
import com.flowfin.core.model.TransactionId
import com.flowfin.core.model.TransactionKind
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.colorKey
import com.flowfin.core.ui.detailDateLabel
import com.flowfin.core.ui.iconKey
import com.flowfin.core.ui.monthShortLabel
import com.flowfin.core.ui.rowKind
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Drives Transaction detail: loads one row, joins its account(s) and category
 * into the money-path rail, and deletes on request. The read is one-shot — a
 * single transaction doesn't change under the screen; a delete leaves it.
 */
class TransactionDetailViewModel(
  private val transactionId: TransactionId,
  private val transactions: TransactionRepository,
  private val accounts: AccountRepository,
  private val categories: CategoryRepository,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val zone = TimeZone.currentSystemDefault()

  private val _uiState = MutableStateFlow<TransactionDetailUiState>(TransactionDetailUiState.Loading)
  val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

  private val effectChannel = Channel<TransactionDetailEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  init {
    viewModelScope.launch { _uiState.value = load() }
  }

  private suspend fun load(): TransactionDetailUiState {
    val txn = transactions.getById(transactionId) ?: return TransactionDetailUiState.NotFound
    val from = txn.fromAccountId?.let { accounts.getById(it) }
    val to = txn.toAccountId?.let { accounts.getById(it) }
    val category = txn.categoryId?.let { categories.getById(it) }
    return txn.toContent(from, to, category)
  }

  fun delete() {
    viewModelScope.launch {
      val effect = when (transactions.delete(transactionId)) {
        is Either.Right -> TransactionDetailEffect.Dismiss
        is Either.Left -> TransactionDetailEffect.ShowMessage(UiText.Res(R.string.tx_detail_delete_error))
      }
      effectChannel.send(effect)
    }
  }

  private fun Transaction.toContent(from: Account?, to: Account?, category: Category?): TransactionDetailUiState.Content {
    val bucket = kind.rowKind()
    val isMove = bucket == RowKind.Transfer
    val isDebt = kind in DEBT_KINDS
    val recorded = recordedAt.toLocalDateTime(zone).date
    return TransactionDetailUiState.Content(
      kindTitle = UiText.Res(kindTitleRes()),
      amountKind = bucket,
      // Internal moves (transfer/allocation/reallocation) net to zero — no +/− sign.
      amountSign = when (bucket) {
        RowKind.Income -> "+"
        RowKind.Expense -> "−"
        RowKind.Transfer -> ""
      },
      currency = money.symbol,
      amountWhole = money.whole(amount),
      amountDecimal = money.fraction(amount),
      // The hero mirrors the subject: the category for income/expense, the move itself otherwise.
      heroIconKey = if (isMove) "sync_alt" else category?.icon,
      heroColorKey = if (isMove) null else category?.color,
      note = note?.takeIf { it.isNotBlank() },
      date = detailDateLabel(recorded),
      edited = updatedAt.takeIf { it > createdAt }?.let {
        val on = it.toLocalDateTime(zone).date
        UiText.Res(R.string.tx_detail_edited, listOf(on.dayOfMonth, UiText.Raw(monthShortLabel(on))))
      },
      from = fromNode(from, category),
      to = toNode(to, category),
      canDelete = !isDebt,
    )
  }

  @StringRes
  private fun Transaction.kindTitleRes(): Int = when (kind) {
    TransactionKind.INCOME -> R.string.tx_detail_kind_income
    TransactionKind.EXPENSE -> R.string.tx_detail_kind_expense
    TransactionKind.TRANSFER -> R.string.tx_detail_kind_transfer
    TransactionKind.ALLOCATION -> R.string.tx_detail_kind_allocation
    TransactionKind.REALLOCATION -> R.string.tx_detail_kind_reallocation
    TransactionKind.DEBT_BORROW, TransactionKind.DEBT_LEND,
    TransactionKind.DEBT_REPAY_OUT, TransactionKind.DEBT_REPAY_IN -> R.string.tx_detail_kind_debt
  }

  private fun Transaction.fromNode(fromAccount: Account?, category: Category?): PathNode? = when (kind) {
    TransactionKind.INCOME -> categoryNode(R.string.tx_detail_role_category, category)
    TransactionKind.EXPENSE -> fromAccount?.let { accountNode(R.string.tx_detail_role_paid_from, it) }
    TransactionKind.TRANSFER, TransactionKind.ALLOCATION ->
      fromAccount?.let { accountNode(R.string.tx_detail_role_from, it) }
    TransactionKind.REALLOCATION -> fromAccount?.let { accountNode(R.string.tx_detail_role_from_envelope, it) }
    TransactionKind.DEBT_LEND, TransactionKind.DEBT_REPAY_OUT ->
      fromAccount?.let { accountNode(R.string.tx_detail_role_paid_from, it) }
    TransactionKind.DEBT_BORROW, TransactionKind.DEBT_REPAY_IN -> debtNode()
  }

  private fun Transaction.toNode(toAccount: Account?, category: Category?): PathNode? = when (kind) {
    TransactionKind.INCOME -> toAccount?.let { accountNode(R.string.tx_detail_role_received_in, it) }
    TransactionKind.EXPENSE -> categoryNode(R.string.tx_detail_role_category, category)
    TransactionKind.TRANSFER -> toAccount?.let { accountNode(R.string.tx_detail_role_to, it) }
    TransactionKind.ALLOCATION, TransactionKind.REALLOCATION ->
      toAccount?.let { accountNode(R.string.tx_detail_role_to_envelope, it) }
    TransactionKind.DEBT_BORROW, TransactionKind.DEBT_REPAY_IN ->
      toAccount?.let { accountNode(R.string.tx_detail_role_received_in, it) }
    TransactionKind.DEBT_LEND, TransactionKind.DEBT_REPAY_OUT -> debtNode()
  }

  private fun accountNode(@StringRes roleRes: Int, account: Account) = PathNode(
    role = UiText.Res(roleRes),
    name = UiText.Raw(account.name),
    iconKey = account.iconKey(),
    colorKey = account.colorKey(),
  )

  private fun categoryNode(@StringRes roleRes: Int, category: Category?) = PathNode(
    role = UiText.Res(roleRes),
    name = category?.name?.let(UiText::Raw) ?: UiText.Res(R.string.tx_uncategorized),
    iconKey = category?.icon,
    colorKey = category?.color,
  )

  // Debt counterparty placeholder — the link to the debt/person lands with Debts (FLO-18).
  private fun debtNode() = PathNode(
    role = UiText.Res(R.string.tx_detail_role_debt),
    name = UiText.Res(R.string.tx_detail_kind_debt),
    iconKey = null,
    colorKey = null,
  )
}

private val DEBT_KINDS = setOf(
  TransactionKind.DEBT_BORROW,
  TransactionKind.DEBT_LEND,
  TransactionKind.DEBT_REPAY_OUT,
  TransactionKind.DEBT_REPAY_IN,
)
