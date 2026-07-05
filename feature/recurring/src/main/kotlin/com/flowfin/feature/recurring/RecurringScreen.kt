package com.flowfin.feature.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
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
  onAdd: () -> Unit = {},
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
        is RecurringUiState.AllPaused -> Notice(
          eyebrow = stringResource(R.string.recurring_all_paused_eyebrow),
          title = stringResource(R.string.recurring_all_paused_title),
          body = pluralStringResource(R.plurals.recurring_all_paused_body, state.pausedCount, state.pausedCount),
        )
        is RecurringUiState.Content -> LazyColumn(
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(start = HORIZONTAL, end = HORIZONTAL, bottom = 96.dp),
        ) {
          if (state.pending.isNotEmpty()) pendingSection(state.pending, onMarkPaid, onSkip)
          upcomingSection(state.upcoming)
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

private fun LazyListScope.upcomingSection(groups: List<RecurringMonthGroup>) {
  if (groups.isEmpty()) return
  item(key = "active-head") {
    SectionHead(
      title = stringResource(R.string.recurring_section_active),
      count = groups.sumOf { it.rows.size },
      aux = stringResource(R.string.recurring_section_active_aux),
    )
  }
  groups.forEachIndexed { index, group ->
    item(key = "month-$index") { MonthHeading(group.label) }
    items(group.rows, key = { it.id.value.toString() }) { UpcomingRow(it) }
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
private fun UpcomingRow(row: RecurringUpcomingUi) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
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

@Composable
private fun FreqTag(freq: UiText) {
  val palette = FlowFinTheme.colors
  Text(
    text = freq.asString().uppercase(),
    modifier = Modifier
      .border(1.dp, palette.border, RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp),
    style = FlowFinTheme.typography.caption.copy(fontSize = 8.5.sp),
    color = palette.textSoft,
  )
}

@Composable
private fun Amount(whole: String, decimal: String) {
  val palette = FlowFinTheme.colors
  Row(verticalAlignment = Alignment.Bottom) {
    Text(whole, style = FlowFinTheme.typography.monoNum.copy(fontSize = 15.sp), color = palette.textMute)
    Text(decimal, style = FlowFinTheme.typography.monoNum.copy(fontSize = 12.sp), color = palette.textFaint)
  }
}

@Composable
private fun IconTile(image: ImageVector, tint: Color) {
  Box(
    modifier = Modifier
      .size(38.dp)
      .background(tint.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
      .border(1.dp, tint.copy(alpha = 0.26f), RoundedCornerShape(10.dp)),
    contentAlignment = Alignment.Center,
  ) {
    Icon(imageVector = image, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
  }
}

/** Centered, CTA-less informational state — shared by the empty and all-paused screens. */
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
    ),
  )
}
