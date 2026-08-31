package com.flowfin.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.DonutSegment
import com.flowfin.core.designsystem.component.FlowFinDonutChart
import com.flowfin.core.designsystem.component.FlowFinIconButton
import com.flowfin.core.designsystem.component.FlowFinScopeTabs
import com.flowfin.core.designsystem.component.FlowFinTrendChart
import com.flowfin.core.designsystem.component.TrendBar
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.resources.R
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor

private val HORIZONTAL = 24.dp

@Composable
fun ReportsScreen(
  state: ReportsUiState,
  modifier: Modifier = Modifier,
  onPreviousMonth: () -> Unit = {},
  onNextMonth: () -> Unit = {},
  onSelectScope: (ReportScope) -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  Column(modifier.fillMaxSize().background(palette.bg)) {
    Text(
      text = stringResource(R.string.reports_title),
      modifier = Modifier
        .statusBarsPadding()
        .padding(start = HORIZONTAL, top = 12.dp),
      style = FlowFinTheme.typography.h2.copy(fontSize = 26.sp),
      color = palette.text,
    )

    when (state) {
      ReportsUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth())
      ReportsUiState.Empty -> EmptyNotice(Modifier.weight(1f))
      is ReportsUiState.Content -> Content(state, onPreviousMonth, onNextMonth, onSelectScope, Modifier.weight(1f))
    }
  }
}

@Composable
private fun Content(
  state: ReportsUiState.Content,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onSelectScope: (ReportScope) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .padding(horizontal = HORIZONTAL),
  ) {
    MonthStrip(state, onPreviousMonth, onNextMonth)
    SummaryCard(state)

    val scopeLabels = ReportScope.entries.associateWith {
      stringResource(if (it == ReportScope.EXPENSE) R.string.add_tx_type_expense else R.string.add_tx_type_income)
    }
    FlowFinScopeTabs(
      options = ReportScope.entries,
      selected = state.scope,
      onSelect = onSelectScope,
      label = { scopeLabels.getValue(it) },
      modifier = Modifier.padding(top = 22.dp),
    )

    if (state.trend != null) {
      Trend(state.trend)
    }
    if (state.breakdown.isEmpty()) {
      PeriodEmpty()
    } else {
      Breakdown(state)
    }
    Spacer(Modifier.height(96.dp))
  }
}

@Composable
private fun MonthStrip(state: ReportsUiState.Content, onPrevious: () -> Unit, onNext: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    FlowFinIconButton(
      onClick = onPrevious,
      icon = FlowFinIcons.ChevronLeft,
      contentDescription = stringResource(R.string.reports_previous_month),
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(state.monthLabel, style = FlowFinTheme.typography.h2.copy(fontSize = 20.sp), color = palette.text)
      Text(state.yearLabel, style = FlowFinTheme.typography.caption, color = palette.textFaint)
    }
    // Hidden rather than disabled past the current month: there is nothing
    // ahead to navigate to, so an inert control would only invite a tap.
    if (state.canGoForward) {
      FlowFinIconButton(
        onClick = onNext,
        icon = FlowFinIcons.ChevronRight,
        contentDescription = stringResource(R.string.reports_next_month),
      )
    } else {
      Spacer(Modifier.size(40.dp))
    }
  }
}

@Composable
private fun SummaryCard(state: ReportsUiState.Content) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(16.dp)
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 18.dp)
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .padding(18.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.reports_net).uppercase(),
      style = FlowFinTheme.typography.caption,
      color = palette.textFaint,
    )
    Row(
      modifier = Modifier.padding(top = 6.dp),
      verticalAlignment = Alignment.Bottom,
    ) {
      Text(
        text = state.netWhole,
        style = FlowFinTheme.typography.monoNum.copy(fontSize = 30.sp),
        color = if (state.netIsPositive) palette.positive else palette.negative,
      )
      Text(
        text = state.netDecimal,
        style = FlowFinTheme.typography.monoNum.copy(fontSize = 15.sp),
        color = palette.textFaint,
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      SplitCell(stringResource(R.string.reports_income), state.incomeTotal, palette.positive)
      SplitCell(stringResource(R.string.reports_expenses), state.expenseTotal, palette.negative)
    }
  }
}

@Composable
private fun SplitCell(label: String, value: String, dot: androidx.compose.ui.graphics.Color) {
  val palette = FlowFinTheme.colors
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(Modifier.size(6.dp).clip(CircleShape).background(dot))
      Spacer(Modifier.width(6.dp))
      Text(label.uppercase(), style = FlowFinTheme.typography.caption, color = palette.textFaint)
    }
    Text(
      text = value,
      modifier = Modifier.padding(top = 4.dp),
      style = FlowFinTheme.typography.monoNum.copy(fontSize = 15.sp),
      color = palette.text,
    )
  }
}

@Composable
private fun Trend(trend: TrendUi) {
  val palette = FlowFinTheme.colors
  Column(Modifier.padding(top = 26.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top,
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          text = trend.title.asString(),
          style = FlowFinTheme.typography.h2.copy(fontSize = 17.sp),
          color = palette.text,
        )
        Text(
          text = trend.paceLabel.asString(),
          modifier = Modifier.padding(top = 2.dp),
          style = FlowFinTheme.typography.caption,
          color = palette.textSoft,
        )
      }
      if (trend.todayAmount != null) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = trend.todayAmount,
            style = FlowFinTheme.typography.monoNum.copy(fontSize = 17.sp),
            color = palette.text,
          )
          Text(
            text = stringResource(R.string.reports_today).uppercase(),
            style = FlowFinTheme.typography.caption,
            color = palette.textFaint,
          )
        }
      }
    }
    FlowFinTrendChart(
      // TrendBar's colour defaults to Unspecified, which draws nothing.
      bars = trend.days.map {
        TrendBar(value = it.value, color = palette.accent, today = it.today, future = it.future)
      },
      modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
      averageLine = trend.paceFraction,
      averageLabel = stringResource(R.string.reports_pace_tick),
    )
  }
}

@Composable
private fun Breakdown(state: ReportsUiState.Content) {
  val palette = FlowFinTheme.colors
  Column(Modifier.padding(top = 30.dp)) {
    Text(
      text = stringResource(R.string.reports_breakdown).uppercase(),
      style = FlowFinTheme.typography.label,
      color = palette.textSoft,
    )
    Box(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      contentAlignment = Alignment.Center,
    ) {
      FlowFinDonutChart(
        segments = state.breakdown.map {
          DonutSegment(value = it.value, color = categoryColor(it.colorKey))
        },
        modifier = Modifier.size(168.dp),
      ) {
        // The slot stacks its children, so the two lines need a column of their
        // own — and mono line boxes are tight enough to collide without a gap.
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = state.breakdownTotal,
            style = FlowFinTheme.typography.monoNum.copy(fontSize = 17.sp),
            color = palette.text,
          )
          Text(
            // The short plural, not reports_breakdown_count: "7 transactions"
            // is wider than the donut's hole and runs onto the ring.
            text = pluralStringResource(R.plurals.reports_txns, state.breakdownCount, state.breakdownCount),
            style = FlowFinTheme.typography.caption,
            color = palette.textFaint,
          )
        }
      }
    }
    Column(modifier = Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      state.breakdown.forEach { BreakdownRow(it) }
    }
  }
}

@Composable
private fun BreakdownRow(row: BreakdownRowUi) {
  val palette = FlowFinTheme.colors
  val tint = categoryColor(row.colorKey)
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(Modifier.size(8.dp).clip(CircleShape).background(tint))
      Spacer(Modifier.width(10.dp))
      Text(
        text = row.name,
        modifier = Modifier.weight(1f),
        style = FlowFinTheme.typography.body,
        color = palette.text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(row.amountWhole, style = FlowFinTheme.typography.monoNum.copy(fontSize = 14.sp), color = palette.text)
      Text(row.amountDecimal, style = FlowFinTheme.typography.monoNum.copy(fontSize = 11.sp), color = palette.textFaint)
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 7.dp)
        .height(4.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(palette.surface3),
    ) {
      Box(
        Modifier
          .fillMaxWidth(row.percent / 100f)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(tint),
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stringResource(R.string.reports_share, row.percent),
        style = FlowFinTheme.typography.caption,
        color = palette.textSoft,
      )
      Text(
        text = pluralStringResource(R.plurals.reports_txns, row.transactionCount, row.transactionCount),
        style = FlowFinTheme.typography.caption,
        color = palette.textFaint,
      )
    }
  }
}

/** The selected month has rows in the other scope, but none in this one. */
@Composable
private fun PeriodEmpty() {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 40.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.reports_period_empty_title),
      style = FlowFinTheme.typography.h2.copy(fontSize = 17.sp),
      color = palette.textMute,
      textAlign = TextAlign.Center,
    )
    Text(
      text = stringResource(R.string.reports_period_empty_body),
      modifier = Modifier.padding(top = 8.dp),
      style = FlowFinTheme.typography.caption,
      color = palette.textSoft,
      textAlign = TextAlign.Center,
    )
  }
}

/** Nothing has ever been recorded — a month picker would be theatre. */
@Composable
private fun EmptyNotice(modifier: Modifier = Modifier) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = HORIZONTAL),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(R.string.reports_empty_eyebrow).uppercase(),
      style = FlowFinTheme.typography.caption,
      color = palette.textFaint,
    )
    Text(
      text = stringResource(R.string.reports_empty_title),
      modifier = Modifier.padding(top = 10.dp),
      style = FlowFinTheme.typography.h2,
      color = palette.textMute,
      textAlign = TextAlign.Center,
    )
    Text(
      text = stringResource(R.string.reports_empty_body),
      modifier = Modifier.padding(top = 10.dp),
      style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
      color = palette.textSoft,
      textAlign = TextAlign.Center,
    )
  }
}

@Preview(name = "Reports", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewReports() = FlowFinTheme {
  ReportsScreen(
    state = ReportsUiState.Content(
      monthLabel = "December",
      yearLabel = "2026",
      canGoForward = false,
      scope = ReportScope.EXPENSE,
      netWhole = "+1,05,000",
      netDecimal = ".00",
      netIsPositive = true,
      incomeTotal = "Rs 1,50,000",
      expenseTotal = "Rs 45,000",
      trend = null,
      breakdown = listOf(
        BreakdownRowUi("Rent", "rent", "20,000", ".00", 44, 1, 2_000_000f),
        BreakdownRowUi("Food & Dining", "food", "9,000", ".00", 20, 14, 900_000f),
        BreakdownRowUi("Transport", "transport", "5,000", ".00", 11, 22, 500_000f),
      ),
      breakdownTotal = "45K",
      breakdownCount = 37,
    ),
  )
}
