package com.flowfin.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinQuickAction
import com.flowfin.core.designsystem.component.FlowFinSectionEmptyHint
import com.flowfin.core.designsystem.component.FlowFinTileIcon
import com.flowfin.core.designsystem.component.FlowFinTransactionRow
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.resources.R
import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon

private val HORIZONTAL = 24.dp

@Composable
internal fun Hero(state: AccountDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  val tint = state.colorKey?.let { categoryColor(it) }
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    FlowFinTileIcon(icon = categoryIcon(state.iconKey), tint = tint, dashed = state.isBudget, size = 56.dp)
    Column(Modifier.weight(1f)) {
      Text(
        text = state.name,
        style = FlowFinTheme.typography.h2.copy(fontSize = 28.sp, fontStyle = FontStyle.Italic, letterSpacing = (-0.02).em),
        color = palette.text,
      )
      Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        TypeTag(state.typeTag.asString(), tint ?: palette.textMute)
        if (state.meta.isNotBlank()) {
          Text(
            text = state.meta.uppercase(),
            style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.14.em),
            color = palette.textMute,
          )
        }
      }
    }
  }
}

/** The hero's "Real" / "Budget" pill, tinted to the account's hue. */
@Composable
private fun TypeTag(text: String, tint: Color) {
  val shape = RoundedCornerShape(6.dp)
  Text(
    text = text.uppercase(),
    modifier = Modifier
      .clip(shape)
      .background(tint.copy(alpha = 0.08f))
      .border(1.dp, tint.copy(alpha = 0.28f), shape)
      .padding(horizontal = 7.dp, vertical = 2.dp),
    style = FlowFinTheme.typography.caption.copy(fontSize = 10.sp, letterSpacing = 0.08.em),
    color = tint,
  )
}

@Composable
internal fun BalanceBlock(state: AccountDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = HORIZONTAL)
      .padding(top = 8.dp, bottom = 18.dp)
      .dashedBottomRule(palette.borderStrong)
      .padding(bottom = 18.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(Modifier.width(14.dp).height(1.dp).background(palette.textSoft))
      Text(
        text = stringResource(R.string.account_detail_balance_label).uppercase(),
        modifier = Modifier.padding(start = 8.dp),
        style = FlowFinTheme.typography.label,
        color = palette.textMute,
      )
      if (state.noActivity) {
        Spacer(Modifier.weight(1f))
        OpeningTag()
      }
    }
    Row(modifier = Modifier.padding(top = 12.dp)) {
      Text(
        text = state.currency,
        modifier = Modifier.alignByBaseline(),
        style = FlowFinTheme.typography.h2.copy(fontSize = 22.sp, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic),
        color = palette.textMute,
      )
      Text(
        text = state.balanceWhole,
        modifier = Modifier.alignByBaseline().padding(start = 8.dp),
        style = FlowFinTheme.typography.hero.copy(fontSize = 52.sp, lineHeight = 52.sp, fontWeight = FontWeight.Light),
        color = palette.text,
      )
      Text(
        text = state.balanceDecimal,
        modifier = Modifier.alignByBaseline().padding(start = 4.dp),
        style = FlowFinTheme.typography.monoNum.copy(fontSize = 22.sp, fontWeight = FontWeight.Light),
        color = palette.textMute,
      )
    }
  }
}

/** The faint "Opening" pill shown when the balance is still just the opening figure. */
@Composable
private fun OpeningTag() {
  val palette = FlowFinTheme.colors
  Text(
    text = stringResource(R.string.account_detail_balance_opening).uppercase(),
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(Color.White.copy(alpha = 0.04f))
      .padding(horizontal = 7.dp, vertical = 2.dp),
    style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.2.em),
    color = palette.textSoft,
  )
}

@Composable
internal fun FlowStrip(flow: FlowUi, currency: String) {
  val palette = FlowFinTheme.colors
  Row(modifier = Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL, vertical = 18.dp)) {
    FlowCell(
      label = stringResource(R.string.account_detail_flow_period, stringResource(R.string.account_detail_flow_in), flow.month),
      dot = palette.positive,
      value = flow.inflow,
      currency = currency,
      valueColor = palette.positive,
      modifier = Modifier.weight(1f),
    )
    FlowDivider()
    FlowCell(
      label = stringResource(R.string.account_detail_flow_period, stringResource(R.string.account_detail_flow_out), flow.month),
      dot = palette.negative,
      value = flow.outflow,
      currency = currency,
      valueColor = palette.text,
      modifier = Modifier.weight(1f),
    )
    FlowDivider()
    FlowCell(
      label = stringResource(R.string.account_detail_flow_net),
      dot = palette.accent,
      value = flow.net,
      currency = currency,
      valueColor = when (flow.netSign) {
        FlowSign.Up -> palette.positive
        FlowSign.Down -> palette.negative
        FlowSign.Flat -> palette.text
      },
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun FlowCell(
  label: String,
  dot: Color,
  value: String,
  currency: String,
  valueColor: Color,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
      Box(Modifier.size(5.dp).clip(CircleShape).background(dot))
      Text(
        text = label.uppercase(),
        style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.16.em),
        color = palette.textSoft,
      )
    }
    Text(
      text = buildAnnotatedString {
        withStyle(SpanStyle(color = palette.textMute, fontStyle = FontStyle.Italic, fontSize = 11.sp)) {
          append("$currency ")
        }
        append(value)
      },
      style = FlowFinTheme.typography.monoNum.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.02).em),
      color = valueColor,
    )
  }
}

@Composable
private fun FlowDivider() {
  Box(Modifier.padding(horizontal = 12.dp).width(1.dp).height(34.dp).background(FlowFinTheme.colors.border))
}

@Composable
internal fun QuickActions(onAddTransaction: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL).padding(top = 22.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    FlowFinQuickAction(
      icon = FlowFinIcons.ArrowUp,
      label = stringResource(R.string.account_detail_qa_income_label),
      sub = stringResource(R.string.account_detail_qa_income_sub),
      tint = palette.positive,
      onClick = onAddTransaction,
      modifier = Modifier.weight(1f),
    )
    FlowFinQuickAction(
      icon = FlowFinIcons.ArrowDown,
      label = stringResource(R.string.account_detail_qa_expense_label),
      sub = stringResource(R.string.account_detail_qa_expense_sub),
      tint = palette.negative,
      onClick = onAddTransaction,
      modifier = Modifier.weight(1f),
    )
    FlowFinQuickAction(
      icon = FlowFinIcons.Transfer,
      label = stringResource(R.string.account_detail_qa_transfer_label),
      sub = stringResource(R.string.account_detail_qa_transfer_sub),
      tint = palette.transfer,
      onClick = onAddTransaction,
      modifier = Modifier.weight(1f),
    )
  }
}

internal fun LazyListScope.activitySection(state: AccountDetailUiState.Content) {
  item(key = "activity-header") { ActivityHeader(awaiting = state.noActivity) }
  if (state.noActivity) {
    item(key = "activity-empty") {
      Box(Modifier.padding(horizontal = HORIZONTAL, vertical = 4.dp)) {
        FlowFinSectionEmptyHint(
          eyebrow = stringResource(R.string.account_detail_empty_eyebrow),
          title = stringResource(R.string.account_detail_empty_title),
          body = stringResource(R.string.account_detail_empty_body),
        )
      }
    }
  } else {
    state.activity.forEach { group ->
      // A group always has ≥1 row, so the first row's id is a stable header key.
      item(key = "date-${group.rows.first().id.value}") { DateHeader(group.dateLabel) }
      group.rows.forEachIndexed { index, row ->
        item(key = "tx-${row.id.value}") { ActivityRow(row, showDivider = index < group.rows.lastIndex) }
      }
    }
  }
}

@Composable
private fun ActivityHeader(awaiting: Boolean) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL).padding(top = 32.dp, bottom = 6.dp),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = stringResource(R.string.account_detail_activity_title),
      style = FlowFinTheme.typography.h2.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light),
      color = palette.text,
    )
    if (awaiting) {
      Text(
        text = stringResource(R.string.account_detail_activity_awaiting).uppercase(),
        modifier = Modifier.padding(bottom = 4.dp),
        style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp, letterSpacing = 0.18.em),
        color = palette.textFaint,
      )
    }
  }
}

@Composable
private fun DateHeader(label: UiText) {
  Text(
    text = label.asString().uppercase(),
    modifier = Modifier.padding(start = HORIZONTAL, end = HORIZONTAL, top = 18.dp, bottom = 4.dp),
    style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.24.em),
    color = FlowFinTheme.colors.textMute,
  )
}

@Composable
private fun ActivityRow(row: TxRowUi, showDivider: Boolean) {
  Column(modifier = Modifier.padding(horizontal = HORIZONTAL)) {
    FlowFinTransactionRow(
      icon = categoryIcon(row.iconKey),
      name = row.name.asString(),
      meta = row.meta,
      amount = row.amount,
      kind = row.kind,
      decimal = row.decimal,
      tint = row.colorKey?.let { categoryColor(it) },
    )
    if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(FlowFinTheme.colors.border))
  }
}

/** A dashed hairline along the bottom edge — the balance block's divider. */
private fun Modifier.dashedBottomRule(color: Color): Modifier = drawBehind {
  drawLine(
    color = color,
    start = Offset(0f, size.height),
    end = Offset(size.width, size.height),
    strokeWidth = 1.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
  )
}
