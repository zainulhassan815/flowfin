package com.flowfin.feature.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinEmptyState
import com.flowfin.core.designsystem.component.FlowFinFilterChips
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSectionEmptyHint
import com.flowfin.core.designsystem.component.FlowFinTransactionRow
import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.TransactionId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon
import kotlin.uuid.Uuid

private val HORIZONTAL = 24.dp

@Composable
fun TransactionsListScreen(
  state: TransactionsListUiState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onFilterChange: (TxFilter) -> Unit = {},
  onTransactionClick: (TransactionId) -> Unit = {},
  onAddTransaction: () -> Unit = {},
) {
  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = stringResource(R.string.transactions_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
      )
    },
  ) {
    when (state) {
      TransactionsListUiState.Loading -> Spacer(Modifier.weight(1f)) // a brief blank; the read resolves immediately
      TransactionsListUiState.Empty -> EmptyLedger(onAddTransaction)
      is TransactionsListUiState.Content -> {
        FilterBar(state.filter, onFilterChange)
        if (state.months.isEmpty()) {
          FilteredEmpty(state.filter)
        } else {
          Ledger(state.months, onTransactionClick)
        }
      }
    }
  }
}

@Composable
private fun ColumnScope.FilterBar(
  selected: TxFilter,
  onSelect: (TxFilter) -> Unit,
) {
  val labels = mapOf(
    TxFilter.All to stringResource(R.string.transactions_filter_all),
    TxFilter.In to stringResource(R.string.transactions_filter_in),
    TxFilter.Out to stringResource(R.string.transactions_filter_out),
    TxFilter.Transfers to stringResource(R.string.transactions_filter_transfers),
  )
  Box(
    Modifier
      .fillMaxWidth()
      .drawBottomRule(FlowFinTheme.colors.border)
      .padding(horizontal = 20.dp, vertical = 12.dp),
  ) {
    FlowFinFilterChips(
      options = TxFilter.entries,
      selected = selected,
      onSelect = onSelect,
      label = { labels.getValue(it) },
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.Ledger(
  months: List<MonthSection>,
  onTransactionClick: (TransactionId) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxWidth().weight(1f),
    contentPadding = PaddingValues(bottom = 12.dp),
  ) {
    months.forEach { month ->
      stickyHeader(key = "month-${month.id}", contentType = "month") { MonthHeader(month) }
      month.days.forEach { day ->
        // A group always has ≥1 row, so the first row's id is a stable header key.
        item(key = "day-${day.rows.first().id.value}", contentType = "day") { DayHeader(day) }
        day.rows.forEachIndexed { index, row ->
          item(key = "tx-${row.id.value}", contentType = "tx") {
            LedgerRow(row, onClick = { onTransactionClick(row.id) }, showDivider = index < day.rows.lastIndex)
          }
        }
      }
    }
    item(key = "bottom-inset") { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)) }
  }
}

@Composable
private fun MonthHeader(month: MonthSection) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(palette.bg) // opaque so rows don't bleed through while pinned
      .padding(horizontal = HORIZONTAL)
      .padding(top = 22.dp, bottom = 10.dp),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = month.label.asString().uppercase(),
      style = FlowFinTheme.typography.caption.copy(fontSize = 11.sp, letterSpacing = 0.22.em),
      color = palette.textMute,
    )
    if (month.net != null) {
      Text(
        text = buildAnnotatedString {
          withStyle(SpanStyle(color = palette.textMute, fontStyle = FontStyle.Italic, fontSize = 11.sp)) {
            append("${month.currency} ")
          }
          append(month.net)
        },
        style = FlowFinTheme.typography.monoNum.copy(fontSize = 11.sp, letterSpacing = (-0.01).em),
        color = if (month.netPositive) palette.positive else palette.textMute,
      )
    }
  }
}

@Composable
private fun DayHeader(day: DayGroup) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.padding(start = HORIZONTAL, end = HORIZONTAL, top = 16.dp, bottom = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(7.dp),
  ) {
    if (day.isToday) {
      Box(Modifier.size(5.dp).clip(CircleShape).background(palette.accent))
    }
    Text(
      text = day.dateLabel.asString().uppercase(),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.24.em),
      color = palette.textSoft,
    )
  }
}

@Composable
private fun LedgerRow(row: TxRowUi, onClick: () -> Unit, showDivider: Boolean) {
  Column(modifier = Modifier.padding(horizontal = HORIZONTAL)) {
    FlowFinTransactionRow(
      icon = categoryIcon(row.iconKey),
      name = row.name.asString(),
      meta = row.meta,
      amount = row.amount,
      kind = row.kind,
      decimal = row.decimal,
      tint = row.colorKey?.let { categoryColor(it) },
      onClick = onClick,
    )
    if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(FlowFinTheme.colors.border))
  }
}

@Composable
private fun ColumnScope.EmptyLedger(onAddTransaction: () -> Unit) {
  Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
    FlowFinEmptyState(
      eyebrow = stringResource(R.string.transactions_empty_eyebrow),
      title = stringResource(R.string.transactions_empty_title),
      body = stringResource(R.string.transactions_empty_body),
      actionLabel = stringResource(R.string.transactions_empty_action),
      onAction = onAddTransaction,
    )
  }
}

@Composable
private fun ColumnScope.FilteredEmpty(filter: TxFilter) {
  val eyebrow = when (filter) {
    TxFilter.In -> R.string.transactions_filter_empty_in
    TxFilter.Out -> R.string.transactions_filter_empty_out
    TxFilter.Transfers -> R.string.transactions_filter_empty_transfers
    // All never reaches here — a non-empty feed always yields at least one month.
    TxFilter.All -> R.string.transactions_empty_eyebrow
  }
  Box(
    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = HORIZONTAL),
    contentAlignment = Alignment.Center,
  ) {
    FlowFinSectionEmptyHint(
      eyebrow = stringResource(eyebrow),
      title = stringResource(R.string.transactions_filter_empty_title),
    )
  }
}

/** A solid hairline along the bottom edge — separates the chips and the month bands. */
private fun Modifier.drawBottomRule(color: Color): Modifier = drawBehind {
  drawLine(
    color = color,
    start = Offset(0f, size.height),
    end = Offset(size.width, size.height),
    strokeWidth = 1.dp.toPx(),
  )
}

@Preview(name = "Transactions · list", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewList() = FlowFinTheme {
  TransactionsListScreen(state = sampleContent(TxFilter.All))
}

@Preview(name = "Transactions · filtered empty", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewFilteredEmpty() = FlowFinTheme {
  TransactionsListScreen(state = TransactionsListUiState.Content(TxFilter.Transfers, emptyList()))
}

@Preview(name = "Transactions · empty", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewEmpty() = FlowFinTheme {
  TransactionsListScreen(state = TransactionsListUiState.Empty)
}

private fun row(name: String, meta: String, amount: String, kind: RowKind, icon: String?, color: String?) = TxRowUi(
  id = TransactionId(Uuid.random()),
  name = UiText.Raw(name),
  meta = meta,
  amount = amount,
  decimal = ".00",
  kind = kind,
  iconKey = icon,
  colorKey = color,
)

private fun sampleContent(filter: TxFilter) = TransactionsListUiState.Content(
  filter = filter,
  months = listOf(
    MonthSection(
      id = "2026-12",
      label = UiText.Raw("December 2026"),
      currency = "Rs",
      net = "+37,500",
      netPositive = true,
      days = listOf(
        DayGroup(
          dateLabel = UiText.Raw("Today · 27 Dec"),
          isToday = true,
          rows = listOf(
            row("ATM withdrawal", "Bank → Cash", "−5,000", RowKind.Transfer, "sync_alt", null),
            row("Lunch with team", "Food · Cash", "−1,250", RowKind.Expense, "restaurant", "food"),
          ),
        ),
        DayGroup(
          dateLabel = UiText.Raw("Yesterday · 26 Dec"),
          isToday = false,
          rows = listOf(
            row("December salary", "Salary · Bank", "+1,50,000", RowKind.Income, "payments", "salary"),
            row("Fuel", "Transport · Cash", "−3,200", RowKind.Expense, "directions_bus", "transport"),
          ),
        ),
      ),
    ),
    MonthSection(
      id = "2026-11",
      label = UiText.Raw("November 2026"),
      currency = "Rs",
      net = "−8,200",
      netPositive = false,
      days = listOf(
        DayGroup(
          dateLabel = UiText.Raw("28 Nov · Fri"),
          isToday = false,
          rows = listOf(
            row("Groceries", "Food · Bank", "−6,800", RowKind.Expense, "restaurant", "food"),
          ),
        ),
      ),
    ),
  ),
)
