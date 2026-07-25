package com.flowfin.feature.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinIconButton
import com.flowfin.core.designsystem.component.FlowFinPendingPaymentCard
import com.flowfin.core.designsystem.component.PaymentStatus
import com.flowfin.core.designsystem.component.PaymentUrgency
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon
import kotlin.uuid.Uuid

private val HORIZONTAL = 24.dp

@Composable
fun RecurringScreen(
  state: RecurringUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onMarkPaid: (RecurringScheduleId, String) -> Unit = { _, _ -> },
  onSkip: (RecurringScheduleId) -> Unit = {},
  onResume: (RecurringScheduleId) -> Unit = {},
  onAdd: () -> Unit = {},
  onOpenDetail: (RecurringScheduleId) -> Unit = {},
) {
  Box(modifier.fillMaxSize().background(FlowFinTheme.colors.bg)) {
    Column(Modifier.fillMaxSize()) {
      Header(state, onAdd)
      when (state) {
        RecurringUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth())
        RecurringUiState.Empty -> Notice(
          eyebrow = stringResource(R.string.recurring_empty_eyebrow),
          title = stringResource(R.string.recurring_empty_title),
          body = stringResource(R.string.recurring_empty_body),
        )
        is RecurringUiState.Content -> LazyColumn(
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(start = HORIZONTAL, end = HORIZONTAL, bottom = 96.dp),
        ) {
          if (state.pending.isNotEmpty()) pendingSection(state.pending, onMarkPaid, onSkip)
          val nothingRunning = state.pendingCount == 0 && state.activeCount == 0 && state.paused.isNotEmpty()
          activeSection(state.activeCount, state.upcoming, nothingRunning, onOpenDetail)
          if (state.paused.isNotEmpty()) pausedSection(state.paused, onResume, onOpenDetail)
        }
      }
    }
    SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
  }
}

@Composable
private fun Header(state: RecurringUiState, onAdd: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(start = HORIZONTAL, end = 16.dp, top = 12.dp, bottom = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.recurring_title),
        style = FlowFinTheme.typography.h2.copy(fontSize = 26.sp),
        color = palette.text,
      )
      if (state is RecurringUiState.Content) {
        // Nothing running (everything paused, or nothing due/upcoming) — the mockup
        // swaps the monthly amount for a plain "paused" pill rather than showing Rs 0.
        val nothingActive = state.pendingCount == 0 && state.activeCount == 0
        Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stringResource(R.string.recurring_stats_monthly_label).uppercase(),
            style = FlowFinTheme.typography.caption.copy(fontSize = 9.5.sp),
            color = palette.textFaint,
          )
          Spacer(Modifier.width(8.dp))
          if (nothingActive && state.paused.isNotEmpty()) PausePill() else Amount(state.monthlyTotalWhole, state.monthlyTotalDecimal)
        }
        Text(
          text = stringResource(R.string.recurring_stats, state.pendingCount, state.activeCount),
          modifier = Modifier.padding(top = 4.dp),
          style = FlowFinTheme.typography.caption,
          color = palette.textSoft,
        )
      }
    }
    FlowFinIconButton(
      onClick = onAdd,
      icon = FlowFinIcons.Add,
      contentDescription = stringResource(R.string.recurring_add_action),
    )
  }
}

@Composable
private fun PausePill() {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .background(palette.surface2, RoundedCornerShape(6.dp))
      .border(1.dp, palette.border, RoundedCornerShape(6.dp))
      .padding(horizontal = 7.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(imageVector = FlowFinIcons.Pause, contentDescription = null, modifier = Modifier.size(9.dp), tint = palette.textMute)
    Spacer(Modifier.width(5.dp))
    Text(
      text = stringResource(R.string.recurring_all_paused_pill),
      style = FlowFinTheme.typography.caption.copy(fontSize = 10.sp),
      color = palette.textMute,
    )
  }
}

private fun LazyListScope.pendingSection(
  pending: List<RecurringPendingUi>,
  onMarkPaid: (RecurringScheduleId, String) -> Unit,
  onSkip: (RecurringScheduleId) -> Unit,
) {
  item(key = "pending-head") {
    SectionHead(
      title = stringResource(R.string.recurring_section_pending),
      count = pending.size,
      aux = stringResource(R.string.recurring_section_pending_aux),
    )
  }
  items(pending, key = { it.id.value.toString() }) { PendingCard(it, onMarkPaid, onSkip) }
}

/**
 * Truly nothing running — no pending, nothing upcoming, but something's paused —
 * surface the "active 0" section with an explanatory hint instead of just going
 * blank. A schedule sitting in Pending still counts as "running" even with
 * nothing upcoming, so [nothingRunning] must already account for that; in that
 * case (or when nothing's paused either — a lone due-today schedule, say) the
 * section is skipped entirely, same as before.
 */
private fun LazyListScope.activeSection(
  activeCount: Int,
  groups: List<RecurringMonthGroup>,
  nothingRunning: Boolean,
  onOpenDetail: (RecurringScheduleId) -> Unit,
) {
  if (groups.isEmpty() && !nothingRunning) return
  item(key = "active-head") {
    SectionHead(
      title = stringResource(R.string.recurring_section_active),
      count = activeCount,
      aux = stringResource(R.string.recurring_section_active_aux),
    )
  }
  if (groups.isEmpty()) {
    item(key = "active-empty-hint") { EmptyActiveHint() }
    return
  }
  groups.forEachIndexed { index, group ->
    item(key = "month-$index") { MonthHeading(group.label) }
    items(group.rows, key = { it.id.value.toString() }) { UpcomingRow(it, onOpenDetail) }
  }
}

private fun LazyListScope.pausedSection(
  paused: List<RecurringPausedUi>,
  onResume: (RecurringScheduleId) -> Unit,
  onOpenDetail: (RecurringScheduleId) -> Unit,
) {
  item(key = "paused-head") {
    SectionHead(stringResource(R.string.recurring_paused_title), paused.size, stringResource(R.string.recurring_paused_aux))
  }
  items(paused, key = { it.id.value.toString() }) { PausedRow(it, onResume, onClick = onOpenDetail) }
}

@Composable
private fun EmptyActiveHint() {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 6.dp, bottom = 8.dp)
      .border(1.dp, palette.borderStrong, RoundedCornerShape(14.dp))
      .padding(vertical = 28.dp, horizontal = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.recurring_all_paused_eyebrow).uppercase(),
      style = FlowFinTheme.typography.caption.copy(fontSize = 9.sp),
      color = palette.textFaint,
    )
    Text(
      text = stringResource(R.string.recurring_all_paused_title),
      modifier = Modifier.padding(top = 6.dp),
      style = FlowFinTheme.typography.h2.copy(fontSize = 19.sp),
      color = palette.text,
    )
    Text(
      text = stringResource(R.string.recurring_all_paused_body),
      modifier = Modifier.padding(top = 6.dp),
      style = FlowFinTheme.typography.body.copy(fontSize = 13.sp),
      color = palette.textMute,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun SectionHead(title: String, count: Int, aux: String) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 6.dp),
    verticalAlignment = Alignment.Bottom,
  ) {
    Text(title, style = FlowFinTheme.typography.h2.copy(fontSize = 18.sp), color = palette.text)
    Spacer(Modifier.width(6.dp))
    Text("$count", style = FlowFinTheme.typography.caption, color = palette.textFaint)
    Spacer(Modifier.weight(1f))
    Text(aux.uppercase(), style = FlowFinTheme.typography.caption, color = palette.textSoft)
  }
}

@Composable
private fun MonthHeading(label: UiText) {
  Text(
    text = label.asString(),
    modifier = Modifier.padding(top = 16.dp, bottom = 2.dp),
    style = FlowFinTheme.typography.caption,
    color = FlowFinTheme.colors.textMute,
  )
}

@Composable
private fun PendingCard(
  row: RecurringPendingUi,
  onMarkPaid: (RecurringScheduleId, String) -> Unit,
  onSkip: (RecurringScheduleId) -> Unit,
) {
  FlowFinPendingPaymentCard(
    icon = categoryIcon(row.iconKey),
    name = row.name,
    schedule = row.schedule.asString(),
    amount = row.amountWhole,
    decimal = row.amountDecimal,
    tint = categoryColor(row.colorKey),
    status = PaymentStatus(
      text = row.status.asString(),
      urgency = if (row.urgency == RecurringUrgency.Late) PaymentUrgency.Late else PaymentUrgency.Due,
    ),
    onSkip = { onSkip(row.id) },
    onPay = { onMarkPaid(row.id, row.name) },
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
  )
}

@Composable
private fun UpcomingRow(row: RecurringUpcomingUi, onClick: (RecurringScheduleId) -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth().clickable { onClick(row.id) }.padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconTile(categoryIcon(row.iconKey), categoryColor(row.colorKey))
    Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(row.name, style = FlowFinTheme.typography.bodyLg, color = palette.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.width(8.dp))
        FreqTag(row.freq)
      }
      Text(
        text = row.due.asString(),
        modifier = Modifier.padding(top = 4.dp),
        style = FlowFinTheme.typography.caption,
        color = palette.textSoft,
      )
    }
    Amount(row.amountWhole, row.amountDecimal)
  }
}

/** Centered, CTA-less informational state — used by the whole-screen empty state. */
@Composable
private fun Notice(eyebrow: String, title: String, body: String) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = HORIZONTAL),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(eyebrow.uppercase(), style = FlowFinTheme.typography.caption, color = palette.textFaint)
    Text(
      text = title,
      modifier = Modifier.padding(top = 10.dp),
      style = FlowFinTheme.typography.h2,
      color = palette.textMute,
    )
    Text(
      text = body,
      modifier = Modifier.padding(top = 10.dp),
      style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
      color = palette.textSoft,
      textAlign = TextAlign.Center,
    )
  }
}

@Preview(name = "Recurring · populated", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecurring() = FlowFinTheme {
  fun id() = RecurringScheduleId(Uuid.random())
  RecurringScreen(
    snackbarHostState = remember { SnackbarHostState() },
    state = RecurringUiState.Content(
      pendingCount = 2,
      activeCount = 4,
      monthlyTotalWhole = "39,500",
      monthlyTotalDecimal = ".00",
      pending = listOf(
        RecurringPendingUi(id(), "Gym Membership", UiText.Raw("Monthly · 25th"), UiText.Raw("Due today"), RecurringUrgency.Due, "5,000", ".00", "favorite", "health"),
        RecurringPendingUi(id(), "Netflix", UiText.Raw("Monthly · 22nd"), UiText.Raw("3 days late"), RecurringUrgency.Late, "1,500", ".00", "subscriptions", "subs"),
      ),
      upcoming = listOf(
        RecurringMonthGroup(
          UiText.Raw("June 2026"),
          listOf(
            RecurringUpcomingUi(id(), "Rent", UiText.Raw("Monthly"), UiText.Raw("Due 1 Jun · in 6 days"), "30,000", ".00", "home", "rent"),
            RecurringUpcomingUi(id(), "Spotify", UiText.Raw("Monthly"), UiText.Raw("Due 5 Jun · in 10 days"), "500", ".00", "subscriptions", "subs"),
          ),
        ),
      ),
      paused = listOf(
        RecurringPausedUi(id(), "Gym", UiText.Raw("Monthly"), UiText.Raw("Paused since 1 Apr"), "5,000", ".00", "health_and_wellness", "health"),
      ),
    ),
  )
}

@Preview(name = "Recurring · all paused", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecurringAllPaused() = FlowFinTheme {
  fun id() = RecurringScheduleId(Uuid.random())
  RecurringScreen(
    snackbarHostState = remember { SnackbarHostState() },
    state = RecurringUiState.Content(
      pendingCount = 0,
      activeCount = 0,
      monthlyTotalWhole = "0",
      monthlyTotalDecimal = ".00",
      pending = emptyList(),
      upcoming = emptyList(),
      paused = listOf(
        RecurringPausedUi(id(), "Rent", UiText.Raw("Monthly"), UiText.Raw("Paused since 12 May"), "30,000", ".00", "home", "rent"),
        RecurringPausedUi(id(), "Netflix", UiText.Raw("Monthly"), UiText.Raw("Paused since 4 May"), "1,500", ".00", "subscriptions", "subs"),
      ),
    ),
  )
}
