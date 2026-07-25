@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinOutlinedButton
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon

private val HORIZONTAL = 24.dp

@Composable
fun RecurringDetailScreen(
  state: RecurringDetailUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onTogglePause: () -> Unit = {},
  onConfirmDelete: () -> Unit = {},
) {
  var confirmDelete by remember { mutableStateOf(false) }
  val content = state as? RecurringDetailUiState.Content

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = content?.name.orEmpty(),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
        actionLabel = if (content != null) stringResource(R.string.recurring_detail_action_delete) else null,
        actionTint = FlowFinTheme.colors.negative,
        onAction = { confirmDelete = true },
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) {
    when (state) {
      RecurringDetailUiState.Loading -> Unit // a brief blank; the read resolves immediately
      RecurringDetailUiState.NotFound -> NotFound()
      is RecurringDetailUiState.Content -> Content(state, onTogglePause)
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
}

@Composable
private fun Content(state: RecurringDetailUiState.Content, onTogglePause: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = HORIZONTAL),
  ) {
    Hero(state)
    FactRail(state)
    if (state.account != null || state.category != null) MoneyPathRail(state)
    PauseResumeButton(state, onTogglePause)
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
  }
}

@Composable
private fun Hero(state: RecurringDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  val tint = categoryColor(state.heroColorKey)
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 22.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    IconTile(image = categoryIcon(state.heroIconKey), tint = tint)
    FlowFinHeroAmount(
      whole = state.amountWhole,
      currency = state.currency,
      decimal = state.amountDecimal,
      tint = palette.text,
    )
    Text(
      text = state.kindTitle.asString(),
      modifier = Modifier.padding(top = 6.dp),
      style = FlowFinTheme.typography.caption,
      color = palette.textSoft,
    )
  }
}

@Composable
private fun FactRail(state: RecurringDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, palette.border, RoundedCornerShape(16.dp))
      .background(palette.surface, RoundedCornerShape(16.dp))
      .padding(horizontal = 16.dp),
  ) {
    FactRow(
      label = stringResource(R.string.recurring_detail_fact_status),
      value = state.status.asString(),
      valueColor = if (state.isActive) palette.positive else palette.textMute,
      divider = true,
    )
    FactRow(
      label = stringResource(R.string.recurring_detail_fact_schedule),
      value = state.scheduleLabel.asString(),
      divider = state.nextDue != null,
    )
    if (state.nextDue != null) {
      FactRow(label = stringResource(R.string.recurring_detail_fact_next_due), value = state.nextDue.asString())
    }
  }
}

@Composable
private fun FactRow(label: String, value: String, divider: Boolean = false, valueColor: Color = FlowFinTheme.colors.text) {
  val palette = FlowFinTheme.colors
  Column {
    Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(label, style = FlowFinTheme.typography.caption, color = palette.textFaint)
      Text(value, style = FlowFinTheme.typography.body.copy(fontSize = 14.sp), color = valueColor)
    }
    if (divider) HorizontalDivider(color = palette.border)
  }
}

@Composable
private fun MoneyPathRail(state: RecurringDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 12.dp)
      .border(1.dp, palette.border, RoundedCornerShape(16.dp))
      .background(palette.surface, RoundedCornerShape(16.dp))
      .padding(vertical = 16.dp, horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    state.account?.let { Node(it, Modifier.weight(1f)) }
    state.category?.let { Node(it, Modifier.weight(1f)) }
  }
}

@Composable
private fun Node(node: RecurringDetailNode, modifier: Modifier = Modifier) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = node.role.asString().uppercase(),
      style = FlowFinTheme.typography.caption.copy(fontSize = 8.5.sp, color = palette.textFaint),
    )
    Spacer(Modifier.height(10.dp))
    IconTile(image = categoryIcon(node.iconKey), tint = categoryColor(node.colorKey))
    Spacer(Modifier.height(10.dp))
    Text(
      text = node.name.asString(),
      style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
      color = palette.text,
      maxLines = 1,
    )
  }
}

@Composable
private fun PauseResumeButton(state: RecurringDetailUiState.Content, onClick: () -> Unit) {
  Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
    if (state.isActive) {
      FlowFinOutlinedButton(
        onClick = onClick,
        text = stringResource(R.string.recurring_detail_action_pause),
        leadingIcon = FlowFinIcons.Pause,
        modifier = Modifier.weight(1f),
      )
    } else {
      FlowFinButton(
        onClick = onClick,
        text = stringResource(R.string.recurring_detail_action_resume),
        leadingIcon = FlowFinIcons.Play,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun DeleteConfirmSheet(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  val palette = FlowFinTheme.colors
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(start = HORIZONTAL, end = HORIZONTAL, bottom = 32.dp, top = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      IconTile(image = FlowFinIcons.Alert, tint = palette.negative)
      Text(
        text = stringResource(R.string.recurring_detail_delete_title),
        modifier = Modifier.padding(top = 16.dp),
        style = FlowFinTheme.typography.h2,
        color = palette.text,
      )
      Text(
        text = stringResource(R.string.recurring_detail_delete_body),
        modifier = Modifier.padding(top = 10.dp),
        style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
        color = palette.textSoft,
        textAlign = TextAlign.Center,
      )
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        FlowFinOutlinedButton(
          onClick = onDismiss,
          text = stringResource(R.string.recurring_detail_delete_cancel),
          modifier = Modifier.weight(1f),
        )
        FlowFinButton(
          onClick = onConfirm,
          text = stringResource(R.string.recurring_detail_delete_confirm),
          destructive = true,
          modifier = Modifier.weight(1f),
        )
      }
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
      text = stringResource(R.string.recurring_detail_not_found_title),
      style = FlowFinTheme.typography.h2.copy(fontSize = 20.sp),
      color = palette.text,
    )
    Text(
      text = stringResource(R.string.recurring_detail_not_found_body),
      modifier = Modifier.padding(top = 8.dp),
      style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
      color = palette.textMute,
    )
  }
}

@Preview(name = "Recurring detail", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecurringDetail() = FlowFinTheme {
  RecurringDetailScreen(
    state = RecurringDetailUiState.Content(
      name = "Netflix",
      kindTitle = UiText.Res(R.string.tx_detail_kind_expense),
      currency = "Rs",
      amountWhole = "1,500",
      amountDecimal = ".00",
      heroIconKey = "subscriptions",
      heroColorKey = "subs",
      isActive = true,
      status = UiText.Raw("Active"),
      scheduleLabel = UiText.Raw("Monthly · 22nd"),
      nextDue = UiText.Raw("Next due 22 Jun 2026"),
      account = RecurringDetailNode(UiText.Res(R.string.tx_detail_role_paid_from), UiText.Raw("Cash"), "wallet", "cash"),
      category = RecurringDetailNode(UiText.Res(R.string.tx_detail_role_category), UiText.Raw("Subscriptions"), "subscriptions", "subs"),
    ),
    snackbarHostState = remember { SnackbarHostState() },
  )
}
