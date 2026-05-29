package com.flowfin.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.flowfin.core.designsystem.component.FlowFinAccountCard
import com.flowfin.core.designsystem.component.FlowFinEmptyState
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinSectionEmptyHint
import com.flowfin.core.designsystem.component.FlowFinTransactionRow
import com.flowfin.core.designsystem.component.TransactionKind
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.TransactionId
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon
import kotlin.uuid.Uuid

@Composable
fun HomeScreen(
  state: HomeUiState,
  modifier: Modifier = Modifier,
  onTransactionClick: (TransactionId) -> Unit = {},
  onAccountClick: (AccountId) -> Unit = {},
) {
  when (state) {
    HomeUiState.Loading -> Box(modifier.fillMaxSize())
    HomeUiState.Empty -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      FlowFinEmptyState(
        eyebrow = "GET STARTED",
        title = "Money lives somewhere.",
        body = "Add your first account to start tracking where it goes.",
        actionLabel = "Add an account",
        onAction = {},
        emphasis = "somewhere",
      )
    }
    is HomeUiState.Content -> HomeContent(state, modifier, onTransactionClick, onAccountClick)
  }
}

@Composable
private fun HomeContent(
  state: HomeUiState.Content,
  modifier: Modifier,
  onTransactionClick: (TransactionId) -> Unit,
  onAccountClick: (AccountId) -> Unit,
) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(vertical = 16.dp),
  ) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
      Text("AVAILABLE BALANCE", style = FlowFinTheme.typography.label, color = palette.textMute)
      Spacer(Modifier.height(12.dp))
      FlowFinHeroAmount(whole = state.totalWhole, decimal = state.totalDecimal)
      Spacer(Modifier.height(8.dp))
      Text("Allocated ${state.allocated}", style = FlowFinTheme.typography.caption, color = palette.textMute)
    }

    SectionLabel("ACCOUNTS")
    Column(
      modifier = Modifier.padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      state.accounts.forEach { account ->
        val tint = if (account.colorKey != null) categoryColor(account.colorKey) else null
        FlowFinAccountCard(
          icon = categoryIcon(account.iconKey),
          name = account.name,
          meta = account.meta,
          balance = account.balanceWhole,
          decimal = account.balanceDecimal,
          tint = tint,
          dashedIcon = account.isBudget,
          modifier = Modifier.fillMaxWidth(),
          onClick = { onAccountClick(account.id) },
        )
      }
    }

    SectionLabel("RECENT")
    if (state.recent.isEmpty()) {
      Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        FlowFinSectionEmptyHint(eyebrow = "NOTHING YET", title = "No activity to show.")
      }
    } else {
      Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        state.recent.forEach { row ->
          val tint = if (row.colorKey != null) categoryColor(row.colorKey) else null
          FlowFinTransactionRow(
            icon = categoryIcon(row.iconKey),
            name = row.name,
            meta = row.meta,
            amount = row.amount,
            kind = row.kind,
            decimal = row.decimal,
            tint = tint,
            onClick = { onTransactionClick(row.id) },
          )
        }
      }
    }
  }
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text,
    style = FlowFinTheme.typography.label,
    color = FlowFinTheme.colors.textMute,
    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
  )
}

@Preview(name = "Home", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 400, heightDp = 820)
@Composable
private fun PreviewHome() = FlowFinTheme {
  HomeScreen(
    state = HomeUiState.Content(
      totalWhole = "2,00,000",
      totalDecimal = ".00",
      allocated = "Rs 10,000.00",
      accounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Bank", "Account", "1,83,000", ".00", "bank", "bank", isBudget = false),
        AccountCardUi(AccountId(Uuid.random()), "Food", "Budget", "10,000", ".00", "wallet", "food", isBudget = true),
      ),
      recent = listOf(
        TxRowUi(TransactionId(Uuid.random()), "Salary", "Bank", "+1,50,000", ".00", TransactionKind.Income, "payments", "salary"),
        TxRowUi(TransactionId(Uuid.random()), "Groceries", "Bank", "−5,000", ".00", TransactionKind.Expense, "shopping_cart", "grocery"),
      ),
    ),
  )
}
