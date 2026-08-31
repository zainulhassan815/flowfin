@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinOutlinedButton
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinPersonAvatar
import com.flowfin.core.designsystem.component.FlowFinProgressBar
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSettingsCard
import com.flowfin.core.designsystem.component.FlowFinSettingsRow
import com.flowfin.core.designsystem.component.FlowFinTimeline
import com.flowfin.core.designsystem.component.FlowFinTimelineItem
import com.flowfin.core.designsystem.component.StatusTag
import com.flowfin.core.designsystem.component.StatusTone
import com.flowfin.core.designsystem.component.TimelineTone
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString

private val HORIZONTAL = 24.dp

/**
 * Debt detail. The record-payment sheet is a child of this screen rather than a
 * route — it reads the debt's live remaining and dismisses back to here.
 */
@Composable
fun DebtDetailScreen(
  state: DebtDetailUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onRecordPayment: () -> Unit = {},
  onToggleSettled: () -> Unit = {},
  onConfirmDelete: () -> Unit = {},
  onCloseSheet: () -> Unit = {},
  onAmountDigits: (String) -> Unit = {},
  onLinkAccountChange: (Boolean) -> Unit = {},
  onAccountSelected: (AccountId) -> Unit = {},
  onNoteChange: (String) -> Unit = {},
  onSave: () -> Unit = {},
) {
  var confirmDelete by remember { mutableStateOf(false) }
  val content = state as? DebtDetailUiState.Content

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = content?.personName.orEmpty(),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
        actionLabel = if (content != null) stringResource(R.string.debt_detail_action_delete) else null,
        actionTint = FlowFinTheme.colors.negative,
        onAction = { confirmDelete = true },
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) {
    when (state) {
      DebtDetailUiState.Loading -> Unit // a brief blank; the read resolves immediately
      DebtDetailUiState.NotFound -> NotFound()
      is DebtDetailUiState.Content -> Content(state, onRecordPayment, onToggleSettled)
    }
  }

  if (confirmDelete && content != null) {
    DeleteConfirmSheet(
      onConfirm = {
        confirmDelete = false
        onConfirmDelete()
      },
      onDismiss = { confirmDelete = false },
    )
  }

  content?.sheet?.let { sheet ->
    RecordPaymentSheet(
      state = sheet,
      onDismiss = onCloseSheet,
      onAmountDigits = onAmountDigits,
      onLinkAccountChange = onLinkAccountChange,
      onAccountSelected = onAccountSelected,
      onNoteChange = onNoteChange,
      onSave = onSave,
    )
  }
}

@Composable
private fun Content(
  state: DebtDetailUiState.Content,
  onRecordPayment: () -> Unit,
  onToggleSettled: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = HORIZONTAL),
  ) {
    PersonHero(state)
    AmountBlock(state)
    if (!state.isSettled) {
      FlowFinButton(
        text = stringResource(R.string.debt_detail_record_payment),
        onClick = onRecordPayment,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
      )
    }
    Timeline(state)
    SecondaryActions(state, onToggleSettled)
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
  }
}

@Composable
private fun PersonHero(state: DebtDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FlowFinPersonAvatar(
      initial = state.personName.take(1).uppercase(),
      tint = palette.avatars.byIndex(state.avatarTintIndex),
    )
    Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = state.personName,
          style = FlowFinTheme.typography.h2.copy(fontSize = 20.sp),
          color = palette.text,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        StatusTag(
          text = if (state.isSettled) {
            stringResource(R.string.debt_detail_settled)
          } else {
            state.directionLabel.asString()
          },
          tone = if (state.isSettled) StatusTone.Positive else StatusTone.Neutral,
        )
      }
      if (state.reason != null) {
        Text(
          text = "“${state.reason}”",
          style = FlowFinTheme.typography.body.copy(fontSize = 13.sp, fontStyle = FontStyle.Italic),
          color = palette.textSoft,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = state.openedLabel.asString(),
        style = FlowFinTheme.typography.caption,
        color = palette.textFaint,
      )
    }
  }
}

@Composable
private fun AmountBlock(state: DebtDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = state.remainingLabel.asString().uppercase(),
      style = FlowFinTheme.typography.caption,
      color = palette.textFaint,
    )
    Spacer(Modifier.padding(top = 6.dp))
    FlowFinHeroAmount(
      whole = state.remainingWhole,
      decimal = state.remainingDecimal,
      tint = if (state.isFullyPaid) palette.positive else palette.text,
    )
    FlowFinProgressBar(
      progress = state.progress,
      modifier = Modifier.padding(top = 20.dp),
      color = if (state.isFullyPaid) palette.positive else palette.accent,
    )
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Stat(stringResource(R.string.debt_detail_stat_original), state.originalAmount, palette.text)
      Stat(stringResource(R.string.debt_detail_stat_paid, state.paidPercent), state.paidAmount, palette.positive, TextAlign.Center)
      Stat(stringResource(R.string.debt_detail_stat_remaining), "${state.remainingWhole}${state.remainingDecimal}", palette.warning, TextAlign.End)
    }
  }
}

@Composable
private fun RowScope.Stat(
  label: String,
  value: String,
  tint: Color,
  align: TextAlign = TextAlign.Start,
) {
  val palette = FlowFinTheme.colors
  Column(Modifier.weight(1f)) {
    Text(
      text = label,
      modifier = Modifier.fillMaxWidth(),
      style = FlowFinTheme.typography.caption,
      color = palette.textFaint,
      textAlign = align,
    )
    Text(
      text = value,
      modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
      style = FlowFinTheme.typography.monoNum.copy(fontSize = 14.sp),
      color = tint,
      textAlign = align,
    )
  }
}

@Composable
private fun Timeline(state: DebtDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  Column(Modifier.padding(top = 28.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      Text(
        text = stringResource(R.string.debt_detail_timeline_title),
        style = FlowFinTheme.typography.h2.copy(fontSize = 17.sp),
        color = palette.text,
      )
      Text(
        text = pluralStringResource(R.plurals.debt_detail_timeline_count, state.paymentCount, state.paymentCount),
        style = FlowFinTheme.typography.caption,
        color = palette.textFaint,
      )
    }
    FlowFinTimeline {
      state.timeline.forEach { item ->
        FlowFinTimelineItem(
          date = item.dateLabel.asString(),
          name = item.title.asString(),
          amount = item.amount,
          decimal = item.decimal,
          meta = item.meta?.asString(),
          tone = if (item.isOrigin) TimelineTone.Origin else TimelineTone.Payment,
        )
      }
    }
  }
}

@Composable
private fun SecondaryActions(state: DebtDetailUiState.Content, onToggleSettled: () -> Unit) {
  FlowFinSettingsCard(Modifier.padding(top = 28.dp)) {
    FlowFinSettingsRow(
      name = stringResource(
        if (state.isSettled) R.string.debt_detail_action_reopen else R.string.debt_detail_action_settle,
      ),
      onClick = onToggleSettled,
    )
  }
}

@Composable
private fun DeleteConfirmSheet(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  val palette = FlowFinTheme.colors
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    Column(Modifier.padding(horizontal = HORIZONTAL, vertical = 8.dp)) {
      Text(
        text = stringResource(R.string.debt_detail_delete_title),
        style = FlowFinTheme.typography.h2,
        color = palette.text,
      )
      Text(
        text = stringResource(R.string.debt_detail_delete_body),
        modifier = Modifier.padding(top = 8.dp),
        style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
        color = palette.textSoft,
      )
      FlowFinButton(
        text = stringResource(R.string.debt_detail_delete_confirm),
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        destructive = true,
      )
      FlowFinOutlinedButton(
        text = stringResource(R.string.action_back),
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp),
      )
    }
  }
}

@Composable
private fun NotFound() {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = HORIZONTAL),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(R.string.debt_detail_not_found),
      style = FlowFinTheme.typography.h2,
      color = palette.textMute,
      textAlign = TextAlign.Center,
    )
  }
}

@Preview(name = "Debt detail · I owe", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewDebtDetail() = FlowFinTheme {
  DebtDetailScreen(
    state = DebtDetailUiState.Content(
      personName = "Ahmed",
      avatarTintIndex = 1,
      reason = "Borrowed for rent",
      openedLabel = UiText.Raw("10 Dec · 17 days ago"),
      isSettled = false,
      directionLabel = UiText.Raw("I Owe"),
      remainingLabel = UiText.Raw("You still owe"),
      remainingWhole = "2,000",
      remainingDecimal = ".00",
      originalAmount = "Rs 8,000.00",
      paidAmount = "Rs 6,000.00",
      paidPercent = 75,
      progress = 0.75f,
      isFullyPaid = false,
      timeline = listOf(
        DebtTimelineItemUi("1", UiText.Raw("Thu · 25 Dec 2026"), UiText.Raw("Payment to Ahmed"), null, "−1,500", ".00", false),
        DebtTimelineItemUi("2", UiText.Raw("Mon · 22 Dec 2026"), UiText.Raw("Payment to Ahmed"), UiText.Raw("rent share"), "−2,500", ".00", false),
        DebtTimelineItemUi("3", UiText.Raw("Mon · 15 Dec 2026"), UiText.Raw("Payment to Ahmed"), null, "−2,000", ".00", false),
        DebtTimelineItemUi("4", UiText.Raw("Wed · 10 Dec 2026"), UiText.Raw("Borrowed from Ahmed"), UiText.Raw("Original amount"), "+8,000", ".00", true),
      ),
      paymentCount = 3,
      sheet = null,
    ),
    snackbarHostState = remember { SnackbarHostState() },
  )
}
