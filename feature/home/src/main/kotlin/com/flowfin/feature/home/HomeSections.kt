package com.flowfin.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinSectionEmptyHint
import com.flowfin.core.designsystem.component.FlowFinTileIcon
import com.flowfin.core.designsystem.component.FlowFinTransactionRow
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.model.TransactionId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon

private val HORIZONTAL = 24.dp

fun LazyListScope.heroItem(state: HomeUiState.Content) {
  item(key = "hero", contentType = "hero") {
    Hero(state.currency, state.totalWhole, state.totalDecimal, state.allocated, state.aside)
  }
}

@Composable
private fun Hero(currency: String, whole: String, decimal: String, allocated: String, aside: HeroAside?) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = HORIZONTAL, end = HORIZONTAL, top = 16.dp, bottom = 24.dp),
  ) {
    HeroLabel(stringResource(R.string.home_hero_available))
    HeroFigure(currency, whole, decimal, dimmed = false)
    HeroMeta(allocated, aside)
  }
}

/** The hero before any account exists: a dimmed Rs 0.00 over a "no accounts yet ·
 *  day one" line, standing in for the live allocated/trend meta. */
@Composable
fun EmptyHero(currency: String) {
  val palette = FlowFinTheme.colors
  val metaStyle = FlowFinTheme.typography.caption.copy(fontSize = 11.sp, letterSpacing = 0.14.em)
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = HORIZONTAL, end = HORIZONTAL, top = 16.dp, bottom = 24.dp),
  ) {
    HeroLabel(stringResource(R.string.home_hero_available))
    HeroFigure(currency = currency, whole = "0", decimal = ".00", dimmed = true)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
        .dashedTopRule(palette.borderStrong)
        .padding(top = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(stringResource(R.string.home_hero_no_accounts).uppercase(), style = metaStyle, color = palette.textFaint)
      Box(Modifier.size(3.dp).clip(CircleShape).background(palette.textFaint))
      Text(stringResource(R.string.home_hero_day_one).uppercase(), style = metaStyle, color = palette.textFaint)
    }
  }
}

@Composable
private fun HeroFigure(currency: String, whole: String, decimal: String, dimmed: Boolean) {
  val palette = FlowFinTheme.colors
  val currencyColor = if (dimmed) palette.textFaint else palette.textMute
  val amountColor = if (dimmed) palette.textSoft else palette.text
  val fractionColor = if (dimmed) palette.textFaint else palette.textMute
  Row(modifier = Modifier.padding(top = 14.dp)) {
    Text(
      text = currency,
      modifier = Modifier.alignByBaseline(),
      style = FlowFinTheme.typography.h2.copy(fontSize = 24.sp, fontWeight = FontWeight.Light),
      color = currencyColor,
    )
    Text(
      text = whole,
      modifier = Modifier.alignByBaseline().padding(start = 10.dp),
      style = FlowFinTheme.typography.hero.copy(fontSize = 56.sp, lineHeight = 56.sp),
      color = amountColor,
    )
    Text(
      text = decimal,
      modifier = Modifier.alignByBaseline().padding(start = 4.dp),
      style = FlowFinTheme.typography.monoNum.copy(fontSize = 24.sp, fontWeight = FontWeight.Light),
      color = fractionColor,
    )
  }
}

@Composable
private fun HeroLabel(text: String) {
  val palette = FlowFinTheme.colors
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.width(14.dp).height(1.dp).background(palette.textSoft))
    Text(
      text = text.uppercase(),
      modifier = Modifier.padding(start = 8.dp),
      style = FlowFinTheme.typography.label,
      color = palette.textMute,
    )
  }
}

@Composable
private fun HeroMeta(allocated: String, aside: HeroAside?) {
  val palette = FlowFinTheme.colors
  val metaStyle = FlowFinTheme.typography.caption.copy(fontSize = 11.sp, letterSpacing = 0.05.em)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp)
      .dashedTopRule(palette.borderStrong)
      .padding(top = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(stringResource(R.string.home_hero_allocated), style = metaStyle, color = palette.textMute)
      Text(allocated, style = metaStyle, color = palette.text)
    }
    when (aside) {
      is HeroAside.Trend -> {
        val trendColor = if (aside.rising) palette.positive else palette.negative
        HeroAsideRow(
          icon = if (aside.rising) FlowFinIcons.TrendUp else FlowFinIcons.TrendDown,
          iconTint = trendColor,
          label = aside.percent,
          labelColor = trendColor,
          trailing = stringResource(R.string.home_hero_trend_suffix),
          trailingItalic = false,
        )
      }
      is HeroAside.Settling -> HeroAsideRow(
        icon = FlowFinIcons.Clock,
        iconTint = palette.textSoft,
        label = aside.dayLabel.asString(),
        labelColor = palette.textSoft,
        trailing = stringResource(R.string.home_hero_settling_suffix),
        trailingItalic = true,
      )
      null -> Unit
    }
  }
}

/** The hero's right aside: a small icon + [label], then a quieter [trailing]
 *  phrase. Shared by the trend and the early-days "building a picture" variants. */
@Composable
private fun HeroAsideRow(
  icon: ImageVector,
  iconTint: Color,
  label: String,
  labelColor: Color,
  trailing: String,
  trailingItalic: Boolean,
) {
  val palette = FlowFinTheme.colors
  val style = FlowFinTheme.typography.caption.copy(fontSize = 11.sp, letterSpacing = 0.05.em)
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(11.dp))
      Text(label, style = style, color = labelColor)
    }
    Text(
      text = trailing,
      style = if (trailingItalic) style.copy(fontStyle = FontStyle.Italic) else style,
      color = palette.textMute,
    )
  }
}

fun LazyListScope.accountsSection(
  state: HomeUiState.Content,
  onAccountClick: (AccountId) -> Unit,
  onAll: () -> Unit,
) {
  item(key = "accounts-header", contentType = "header") {
    SectionHeader(title = stringResource(R.string.home_section_accounts), onAll = onAll)
  }
  segment(R.string.home_segment_real, state.realTotal, state.realAccounts, onAccountClick)
  segment(R.string.home_segment_budget, state.budgetTotal, state.budgetAccounts, onAccountClick)
}

private fun LazyListScope.segment(
  @StringRes labelRes: Int,
  total: String,
  rows: List<AccountCardUi>,
  onAccountClick: (AccountId) -> Unit,
) {
  if (rows.isEmpty()) return
  item(key = "segment-$labelRes", contentType = "segment") {
    SegmentLabel(stringResource(labelRes), total)
  }
  itemsIndexed(
    items = rows,
    key = { _, row -> "acct-${row.id.value}" },
    contentType = { _, _ -> "account" },
  ) { index, row ->
    AccountRow(row, onClick = { onAccountClick(row.id) }, showDivider = index < rows.lastIndex)
  }
}

@Composable
private fun SegmentLabel(label: String, total: String) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = HORIZONTAL)
      .padding(top = 12.dp, bottom = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = label.uppercase(),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.24.em),
      color = palette.textMute,
    )
    Text(
      text = total,
      style = FlowFinTheme.typography.caption.copy(fontSize = 11.sp, letterSpacing = 0.05.em),
      color = palette.text,
    )
    Box(Modifier.weight(1f).height(1.dp).background(palette.borderStrong))
  }
}

@Composable
private fun AccountRow(ui: AccountCardUi, onClick: () -> Unit, showDivider: Boolean) {
  val palette = FlowFinTheme.colors
  Column(modifier = Modifier.padding(horizontal = HORIZONTAL)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      FlowFinTileIcon(
        icon = categoryIcon(ui.iconKey),
        tint = ui.colorKey?.let { categoryColor(it) },
        dashed = ui.isBudget,
        size = 38.dp,
      )
      Column(Modifier.weight(1f)) {
        Text(ui.name, style = FlowFinTheme.typography.bodyLg, color = palette.text)
        if (ui.meta.isNotBlank()) {
          Text(
            text = ui.meta.uppercase(),
            modifier = Modifier.padding(top = 4.dp),
            style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.12.em),
            color = palette.textMute,
          )
        }
      }
      Text(
        text = buildAnnotatedString {
          append(ui.balanceWhole)
          withStyle(SpanStyle(color = palette.textMute, fontWeight = FontWeight.Normal)) {
            append(ui.balanceDecimal)
          }
        },
        style = FlowFinTheme.typography.monoNum.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
        color = palette.text,
      )
    }
    if (showDivider) RowDivider()
  }
}

fun LazyListScope.pendingSection(
  state: HomeUiState.Content,
  onPay: (RecurringScheduleId) -> Unit,
  onAll: () -> Unit,
) {
  if (state.pending.isEmpty()) return
  item(key = "pending-header", contentType = "header") {
    SectionHeader(title = stringResource(R.string.home_section_pending), count = state.pendingTotal, onAll = onAll)
  }
  item(key = "pending-strip", contentType = "pending") {
    PendingStrip(state.pending, onPay)
  }
}

@Composable
private fun PendingStrip(rows: List<PendingRowUi>, onPay: (RecurringScheduleId) -> Unit) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(14.dp)
  Box(modifier = Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL, vertical = 8.dp)) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(palette.warning.copy(alpha = 0.04f))
        .border(1.dp, palette.warning.copy(alpha = 0.15f), shape)
        .padding(4.dp),
    ) {
      rows.forEachIndexed { index, row ->
        PendingRow(row, onPay = { onPay(row.id) }, showDivider = index < rows.lastIndex)
      }
    }
  }
}

@Composable
private fun PendingRow(ui: PendingRowUi, onPay: () -> Unit, showDivider: Boolean) {
  val palette = FlowFinTheme.colors
  val markerColor = if (ui.urgency == PendingUrgency.Due) palette.warning else palette.negative
  val status = ui.statusText.asString()
  Column {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Box(Modifier.width(6.dp).height(36.dp).clip(RoundedCornerShape(3.dp)).background(markerColor))
      Column(Modifier.weight(1f)) {
        Text(ui.name, style = FlowFinTheme.typography.bodyLg.copy(fontSize = 16.sp), color = palette.text)
        Text(
          text = buildAnnotatedString {
            append(ui.amountAccount)
            append(" · ")
            withStyle(SpanStyle(color = markerColor)) { append(status) }
          },
          modifier = Modifier.padding(top = 4.dp),
          style = FlowFinTheme.typography.caption.copy(
            fontSize = 11.sp,
            letterSpacing = 0.04.em,
            fontWeight = FontWeight.Normal,
          ),
          color = palette.textMute,
        )
      }
      PayButton(onClick = onPay)
    }
    if (showDivider) {
      Box(Modifier.fillMaxWidth().height(1.dp).background(palette.warning.copy(alpha = 0.08f)))
    }
  }
}

@Composable
private fun PayButton(onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(8.dp)
  Box(
    modifier = Modifier
      .clip(shape)
      .border(1.dp, palette.borderStrong, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 13.dp, vertical = 9.dp),
  ) {
    Text(
      text = stringResource(R.string.home_pending_pay).uppercase(),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.16.em, fontWeight = FontWeight.SemiBold),
      color = palette.text,
    )
  }
}

fun LazyListScope.recentSection(
  state: HomeUiState.Content,
  onTransactionClick: (TransactionId) -> Unit,
  onAll: () -> Unit,
) {
  val recent = state.recent
  item(key = "recent-header", contentType = "header") {
    SectionHeader(
      title = stringResource(R.string.home_section_recent),
      // "History" once entries have aged out of this week; no link when nothing was ever logged.
      linkLabel = stringResource(if (recent is RecentSection.Quiet) R.string.home_link_history else R.string.home_link_all),
      onAll = if (recent is RecentSection.NoEntries) null else onAll,
    )
  }
  when (recent) {
    is RecentSection.Activity -> recent.groups.forEach { group ->
      // A group always has ≥1 row, so the first row's id is a stable, unique header key.
      item(key = "date-${group.rows.first().id.value}", contentType = "date") {
        RecentDateHeader(group.dateLabel.asString())
      }
      itemsIndexed(
        items = group.rows,
        key = { _, row -> "tx-${row.id.value}" },
        contentType = { _, _ -> "transaction" },
      ) { index, row ->
        Column(modifier = Modifier.padding(horizontal = HORIZONTAL)) {
          FlowFinTransactionRow(
            icon = categoryIcon(row.iconKey),
            name = row.name.asString(),
            meta = row.meta,
            amount = row.amount,
            kind = row.kind,
            decimal = row.decimal,
            tint = row.colorKey?.let { categoryColor(it) },
            onClick = { onTransactionClick(row.id) },
          )
          if (index < group.rows.lastIndex) RowDivider()
        }
      }
    }

    RecentSection.NoEntries -> recentEmptyHint(
      eyebrow = UiText.Res(R.string.home_recent_empty_eyebrow),
      title = UiText.Res(R.string.home_recent_empty_title),
      body = UiText.Res(R.string.home_recent_empty_body),
    )

    is RecentSection.Quiet -> recentEmptyHint(
      eyebrow = UiText.Res(R.string.home_recent_quiet_eyebrow),
      title = UiText.Res(R.string.home_recent_quiet_title),
      body = UiText.Res(R.string.home_recent_quiet_body, listOf(recent.lastEntry)),
    )
  }
}

private fun LazyListScope.recentEmptyHint(eyebrow: UiText, title: UiText, body: UiText) {
  item(key = "recent-empty", contentType = "empty") {
    Box(Modifier.padding(horizontal = HORIZONTAL, vertical = 4.dp)) {
      FlowFinSectionEmptyHint(eyebrow = eyebrow.asString(), title = title.asString(), body = body.asString())
    }
  }
}

@Composable
private fun RecentDateHeader(label: String) {
  Text(
    text = label.uppercase(),
    modifier = Modifier.padding(start = HORIZONTAL, end = HORIZONTAL, top = 18.dp, bottom = 4.dp),
    style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.24.em),
    color = FlowFinTheme.colors.textMute,
  )
}

@Composable
private fun SectionHeader(
  title: String,
  count: Int? = null,
  linkLabel: String = stringResource(R.string.home_link_all),
  onAll: (() -> Unit)? = null,
) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = HORIZONTAL)
      .padding(top = 24.dp, bottom = 14.dp),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = title,
        style = FlowFinTheme.typography.h2.copy(fontSize = 24.sp),
        color = palette.text,
      )
      if (count != null) {
        Text(
          text = "($count)",
          modifier = Modifier.padding(bottom = 2.dp),
          style = FlowFinTheme.typography.monoNum.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal),
          color = palette.textMute,
        )
      }
    }
    if (onAll != null) {
      Row(
        modifier = Modifier.clickable(onClick = onAll),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = linkLabel.uppercase(),
          style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.18.em),
          color = palette.textMute,
        )
        Icon(
          imageVector = FlowFinIcons.Forward,
          contentDescription = null,
          tint = palette.textMute,
          modifier = Modifier.size(14.dp),
        )
      }
    }
  }
}

@Composable
private fun RowDivider() {
  Box(Modifier.fillMaxWidth().height(1.dp).background(FlowFinTheme.colors.border))
}

/** A dashed hairline along the top edge — the hero's meta divider. */
private fun Modifier.dashedTopRule(color: Color): Modifier = drawBehind {
  drawLine(
    color = color,
    start = Offset(0f, 0f),
    end = Offset(size.width, 0f),
    strokeWidth = 1.dp.toPx(),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
  )
}
