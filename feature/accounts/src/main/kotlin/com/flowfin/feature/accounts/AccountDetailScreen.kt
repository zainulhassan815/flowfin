package com.flowfin.feature.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.resources.R
import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString

private val HORIZONTAL = 24.dp

@Composable
fun AccountDetailScreen(
  state: AccountDetailUiState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onAddTransaction: () -> Unit = {},
) {
  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = (state as? AccountDetailUiState.Content)?.typeLabel?.asString().orEmpty(),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
      )
    },
  ) {
    when (state) {
      AccountDetailUiState.Loading -> Unit // a brief blank; the read resolves immediately
      AccountDetailUiState.NotFound -> NotFound()
      is AccountDetailUiState.Content -> Content(state, onAddTransaction)
    }
  }
}

@Composable
private fun Content(state: AccountDetailUiState.Content, onAddTransaction: () -> Unit) {
  LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)) {
    item(key = "hero") { Hero(state) }
    item(key = "balance") { BalanceBlock(state) }
    if (state.flow != null) item(key = "flow") { FlowStrip(state.flow, state.currency) }
    item(key = "quick") { QuickActions(onAddTransaction) }
    activitySection(state)
    // Last row clears the gesture bar (this is a pushed screen — no nav bar below).
    item(key = "bottom-inset") { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)) }
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
      text = stringResource(R.string.account_detail_not_found_title),
      style = FlowFinTheme.typography.h2.copy(fontSize = 20.sp, fontStyle = FontStyle.Italic),
      color = palette.text,
    )
    Text(
      text = stringResource(R.string.account_detail_not_found_body),
      modifier = Modifier.padding(top = 8.dp),
      style = FlowFinTheme.typography.body.copy(fontSize = 14.sp),
      color = palette.textMute,
    )
  }
}

@Preview(name = "Account detail · real", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAccountDetail() = FlowFinTheme {
  AccountDetailScreen(state = sampleContent(noActivity = false))
}

@Preview(name = "Account detail · just created", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAccountDetailEmpty() = FlowFinTheme {
  AccountDetailScreen(state = sampleContent(noActivity = true))
}

private fun sampleContent(noActivity: Boolean) = AccountDetailUiState.Content(
  name = "Bank",
  typeLabel = UiText.Res(R.string.account_detail_type_real),
  typeTag = UiText.Res(R.string.account_detail_tag_real),
  meta = "",
  iconKey = "bank",
  colorKey = "bank",
  isBudget = false,
  currency = "Rs",
  balanceWhole = if (noActivity) "25,000" else "40,000",
  balanceDecimal = ".00",
  noActivity = noActivity,
  flow = if (noActivity) null else FlowUi("Dec", "1,50,000", "1,10,000", "+40,000", FlowSign.Up),
  activity = if (noActivity) emptyList() else listOf(
    TxGroup(
      dateLabel = UiText.Raw("Today · 27 Dec"),
      rows = listOf(
        TxRowUi(
          id = com.flowfin.core.model.TransactionId(kotlin.uuid.Uuid.random()),
          name = UiText.Raw("December salary"),
          meta = "Bank",
          amount = "+1,50,000",
          decimal = ".00",
          kind = com.flowfin.core.designsystem.component.TransactionKind.Income,
          iconKey = "payments",
          colorKey = "salary",
        ),
        TxRowUi(
          id = com.flowfin.core.model.TransactionId(kotlin.uuid.Uuid.random()),
          name = UiText.Raw("Transfer"),
          meta = "Bank → Cash",
          amount = "−5,000",
          decimal = ".00",
          kind = com.flowfin.core.designsystem.component.TransactionKind.Transfer,
          iconKey = "sync_alt",
          colorKey = null,
        ),
      ),
    ),
  ),
)
