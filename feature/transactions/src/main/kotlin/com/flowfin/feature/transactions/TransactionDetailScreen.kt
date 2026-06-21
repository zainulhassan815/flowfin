@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinOutlinedButton
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.TransactionKind as RowKind
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon

private val HORIZONTAL = 24.dp

@Composable
fun TransactionDetailScreen(
  state: TransactionDetailUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onConfirmDelete: () -> Unit = {},
) {
  var confirmDelete by remember { mutableStateOf(false) }
  val content = state as? TransactionDetailUiState.Content

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = content?.kindTitle?.asString().orEmpty(),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
        // Delete lives in the header's destructive slot; it opens a confirm sheet.
        // Hidden for debt rows — they can't be deleted here (see UiState.canDelete).
        actionLabel = if (content?.canDelete == true) stringResource(R.string.tx_detail_action_delete) else null,
        actionTint = FlowFinTheme.colors.negative,
        onAction = { confirmDelete = true },
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) {
    when (state) {
      TransactionDetailUiState.Loading -> Unit // a brief blank; the read resolves immediately
      TransactionDetailUiState.NotFound -> NotFound()
      is TransactionDetailUiState.Content -> Content(state)
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
private fun Content(state: TransactionDetailUiState.Content) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = HORIZONTAL),
  ) {
    Hero(state)
    Rail(state)
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
  }
}

@Composable
private fun Hero(state: TransactionDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  val tint = if (state.amountKind == RowKind.Transfer) palette.transfer else categoryColor(state.heroColorKey)
  val amountColor = when (state.amountKind) {
    RowKind.Income -> palette.positive
    RowKind.Transfer -> palette.transfer
    RowKind.Expense -> palette.text
  }

  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 26.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    IconTile(image = categoryIcon(state.heroIconKey), tint = tint, size = 60.dp, corner = 16.dp, icon = 28.dp)
    FlowFinHeroAmount(
      whole = state.amountSign + state.amountWhole,
      currency = state.currency,
      decimal = state.amountDecimal,
      tint = amountColor,
    )
    if (state.note != null) {
      Text(
        text = state.note,
        modifier = Modifier.padding(top = 14.dp),
        style = FlowFinTheme.typography.h2.copy(fontSize = 21.sp),
        color = palette.text,
        textAlign = TextAlign.Center,
      )
    }
    Text(
      text = state.date.asString(),
      modifier = Modifier.padding(top = if (state.note != null) 8.dp else 18.dp),
      style = FlowFinTheme.typography.caption,
      color = palette.textSoft,
    )
    if (state.edited != null) {
      Text(
        text = state.edited.asString(),
        modifier = Modifier.padding(top = 5.dp),
        style = FlowFinTheme.typography.body.copy(fontSize = 12.sp, fontStyle = FontStyle.Italic),
        color = palette.textFaint,
      )
    }
  }
}

@Composable
private fun Rail(state: TransactionDetailUiState.Content) {
  val palette = FlowFinTheme.colors
  val arrowTint = if (state.amountKind == RowKind.Transfer) palette.transfer else categoryColor(state.heroColorKey)
  if (state.from == null && state.to == null) return

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, palette.border, RoundedCornerShape(16.dp))
      .background(palette.surface, RoundedCornerShape(16.dp))
      .padding(vertical = 16.dp, horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    state.from?.let { Node(it, Modifier.weight(1f)) }
    if (state.from != null && state.to != null) {
      Icon(
        imageVector = FlowFinIcons.ArrowRight,
        contentDescription = null,
        modifier = Modifier.size(22.dp),
        tint = arrowTint,
      )
    }
    state.to?.let { Node(it, Modifier.weight(1f)) }
  }
}

@Composable
private fun Node(node: PathNode, modifier: Modifier = Modifier) {
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
    IconTile(image = categoryIcon(node.iconKey), tint = categoryColor(node.colorKey), size = 38.dp, corner = 10.dp, icon = 18.dp)
    Spacer(Modifier.height(10.dp))
    Text(
      text = node.name.asString(),
      style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
      color = palette.text,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun IconTile(image: ImageVector, tint: Color, size: Dp, corner: Dp, icon: Dp) {
  Box(
    modifier = Modifier
      .size(size)
      .background(tint.copy(alpha = 0.10f), RoundedCornerShape(corner))
      .border(1.dp, tint.copy(alpha = 0.28f), RoundedCornerShape(corner)),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = image,
      contentDescription = null,
      modifier = Modifier.size(icon),
      tint = tint,
    )
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
      IconTile(image = FlowFinIcons.Alert, tint = palette.negative, size = 52.dp, corner = 14.dp, icon = 22.dp)
      Text(
        text = stringResource(R.string.tx_detail_delete_title),
        modifier = Modifier.padding(top = 16.dp),
        style = FlowFinTheme.typography.h2,
        color = palette.text,
      )
      Text(
        text = stringResource(R.string.tx_detail_delete_body),
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
          text = stringResource(R.string.tx_detail_delete_cancel),
          modifier = Modifier.weight(1f),
        )
        FlowFinButton(
          onClick = onConfirm,
          text = stringResource(R.string.tx_detail_delete_confirm),
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
      text = stringResource(R.string.tx_detail_not_found_title),
      style = FlowFinTheme.typography.h2.copy(fontSize = 20.sp),
      color = palette.text,
    )
    Text(
      text = stringResource(R.string.tx_detail_not_found_body),
      modifier = Modifier.padding(top = 8.dp),
      style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
      color = palette.textMute,
    )
  }
}

@Preview(name = "Transaction detail · expense", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewTransactionDetail() = FlowFinTheme {
  TransactionDetailScreen(
    state = TransactionDetailUiState.Content(
      kindTitle = UiText.Res(R.string.tx_detail_kind_expense),
      amountKind = RowKind.Expense,
      amountSign = "−",
      currency = "Rs",
      amountWhole = "1,250",
      amountDecimal = ".00",
      heroIconKey = "restaurant",
      heroColorKey = "food",
      note = "Lunch with the team",
      date = UiText.Raw("Sat · 27 Dec 2026"),
      edited = UiText.Raw("Edited · 2 Jan"),
      from = PathNode(UiText.Res(R.string.tx_detail_role_paid_from), UiText.Raw("Cash"), "wallet", "cash"),
      to = PathNode(UiText.Res(R.string.tx_detail_role_category), UiText.Raw("Food & Dining"), "restaurant", "food"),
      canDelete = true,
    ),
    snackbarHostState = remember { SnackbarHostState() },
  )
}
