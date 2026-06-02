package com.flowfin.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.component.FlowFinEmptyState
import com.flowfin.core.designsystem.component.FlowFinIconButton
import com.flowfin.core.designsystem.component.TransactionKind
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.model.TransactionId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.TxRowUi
import com.flowfin.core.ui.UiText
import kotlin.uuid.Uuid

@Composable
fun HomeScreen(
  state: HomeUiState,
  modifier: Modifier = Modifier,
  onTransactionClick: (TransactionId) -> Unit = {},
  onAccountClick: (AccountId) -> Unit = {},
  onPayPending: (RecurringScheduleId) -> Unit = {},
  onSearch: () -> Unit = {},
  onSettings: () -> Unit = {},
  onAddAccount: () -> Unit = {},
  onAllAccounts: () -> Unit = {},
  onAllPending: () -> Unit = {},
  onAllRecent: () -> Unit = {},
) {
  Column(modifier.fillMaxSize().background(FlowFinTheme.colors.bg)) {
    HomeTopBar(onSearch = onSearch, onSettings = onSettings)
    when (state) {
      HomeUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth())
      is HomeUiState.Empty -> Column(Modifier.weight(1f).fillMaxWidth()) {
        EmptyHero(currency = state.currency)
        Box(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          contentAlignment = Alignment.Center,
        ) {
          FlowFinEmptyState(
            eyebrow = stringResource(R.string.home_empty_eyebrow),
            title = stringResource(R.string.home_empty_title),
            body = stringResource(R.string.home_empty_body),
            actionLabel = stringResource(R.string.home_empty_action),
            onAction = onAddAccount,
            emphasis = stringResource(R.string.home_empty_emphasis),
          )
        }
      }
      is HomeUiState.Content -> HomeContent(
        state = state,
        modifier = Modifier.weight(1f),
        onTransactionClick = onTransactionClick,
        onAccountClick = onAccountClick,
        onPayPending = onPayPending,
        onAllAccounts = onAllAccounts,
        onAllPending = onAllPending,
        onAllRecent = onAllRecent,
      )
    }
  }
}

@Composable
private fun HomeContent(
  state: HomeUiState.Content,
  modifier: Modifier = Modifier,
  onTransactionClick: (TransactionId) -> Unit,
  onAccountClick: (AccountId) -> Unit,
  onPayPending: (RecurringScheduleId) -> Unit,
  onAllAccounts: () -> Unit,
  onAllPending: () -> Unit,
  onAllRecent: () -> Unit,
) {
  LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 96.dp)) {
    heroItem(state)
    accountsSection(state, onAccountClick, onAllAccounts)
    pendingSection(state, onPayPending, onAllPending)
    recentSection(state, onTransactionClick, onAllRecent)
  }
}

@Composable
private fun HomeTopBar(onSearch: () -> Unit, onSettings: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(start = 24.dp, end = 14.dp, top = 8.dp, bottom = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "flowfin",
        style = FlowFinTheme.typography.h2.copy(fontWeight = FontWeight.Light),
        color = palette.text,
      )
      Box(
        modifier = Modifier
          .padding(start = 3.dp)
          .size(5.dp)
          .clip(CircleShape)
          .background(palette.accent),
      )
    }
    Spacer(Modifier.weight(1f))
    FlowFinIconButton(
      onClick = onSearch,
      icon = FlowFinIcons.Search,
      contentDescription = stringResource(R.string.home_action_search),
    )
    FlowFinIconButton(
      onClick = onSettings,
      icon = FlowFinIcons.Settings,
      contentDescription = stringResource(R.string.home_action_settings),
    )
  }
}

@Preview(name = "Home", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewHome() = FlowFinTheme {
  HomeScreen(
    state = HomeUiState.Content(
      currency = "Rs",
      totalWhole = "47,000",
      totalDecimal = ".00",
      allocated = "Rs 16,000",
      aside = HeroAside.Trend(percent = "+12%", rising = true),
      realTotal = "Rs 47,000",
      realAccounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Bank", "", "40,000", ".00", "bank", "bank", isBudget = false),
        AccountCardUi(AccountId(Uuid.random()), "Cash", "", "7,000", ".00", "wallet", "cash", isBudget = false),
      ),
      budgetTotal = "Rs 16,000",
      budgetAccounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Food", "Bank", "9,000", ".00", "restaurant", "food", isBudget = true),
        AccountCardUi(AccountId(Uuid.random()), "Transport", "Bank", "4,500", ".00", "directions_bus", "transport", isBudget = true),
        AccountCardUi(AccountId(Uuid.random()), "Fun", "Bank", "2,500", ".00", "movie", "fun", isBudget = true),
      ),
      pending = listOf(
        PendingRowUi(RecurringScheduleId(Uuid.random()), "Gym Membership", "Rs 5,000 · Bank", UiText.Raw("Due today"), PendingUrgency.Due),
        PendingRowUi(RecurringScheduleId(Uuid.random()), "Netflix", "Rs 1,500 · Bank", UiText.Raw("3 days late"), PendingUrgency.Late),
      ),
      pendingTotal = 2,
      recent = RecentSection.Activity(
        listOf(
          RecentGroup(
            dateLabel = UiText.Raw("Today · 27 Dec"),
            rows = listOf(
              TxRowUi(TransactionId(Uuid.random()), UiText.Raw("Mess Lunch"), "Food", "−500", ".00", TransactionKind.Expense, "restaurant", "food"),
              TxRowUi(TransactionId(Uuid.random()), UiText.Raw("ATM Withdrawal"), "Bank → Cash", "5,000", ".00", TransactionKind.Transfer, "sync_alt", null),
            ),
          ),
          RecentGroup(
            dateLabel = UiText.Raw("Yesterday · 26 Dec"),
            rows = listOf(
              TxRowUi(TransactionId(Uuid.random()), UiText.Raw("December Salary"), "Bank", "+150,000", ".00", TransactionKind.Income, "payments", "salary"),
            ),
          ),
        ),
      ),
    ),
  )
}

@Preview(name = "Home · empty", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewHomeEmpty() = FlowFinTheme {
  HomeScreen(state = HomeUiState.Empty(currency = "Rs"))
}

@Preview(name = "Home · no entries", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewHomeNoEntries() = FlowFinTheme {
  HomeScreen(
    state = HomeUiState.Content(
      currency = "Rs",
      totalWhole = "47,000",
      totalDecimal = ".00",
      allocated = "Rs 16,000",
      aside = HeroAside.Settling(dayLabel = UiText.Raw("Day 1")),
      realTotal = "Rs 47,000",
      realAccounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Bank", "", "40,000", ".00", "bank", "bank", isBudget = false),
        AccountCardUi(AccountId(Uuid.random()), "Cash", "", "7,000", ".00", "wallet", "cash", isBudget = false),
      ),
      budgetTotal = "Rs 16,000",
      budgetAccounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Food", "Bank", "16,000", ".00", "restaurant", "food", isBudget = true),
      ),
      pending = emptyList(),
      pendingTotal = 0,
      recent = RecentSection.NoEntries,
    ),
  )
}

@Preview(name = "Home · quiet week", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewHomeQuiet() = FlowFinTheme {
  HomeScreen(
    state = HomeUiState.Content(
      currency = "Rs",
      totalWhole = "47,000",
      totalDecimal = ".00",
      allocated = "Rs 16,000",
      aside = HeroAside.Trend(percent = "+12%", rising = true),
      realTotal = "Rs 47,000",
      realAccounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Bank", "", "40,000", ".00", "bank", "bank", isBudget = false),
        AccountCardUi(AccountId(Uuid.random()), "Cash", "", "7,000", ".00", "wallet", "cash", isBudget = false),
      ),
      budgetTotal = "Rs 16,000",
      budgetAccounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Food", "Bank", "16,000", ".00", "restaurant", "food", isBudget = true),
      ),
      pending = emptyList(),
      pendingTotal = 0,
      recent = RecentSection.Quiet(lastEntry = UiText.Raw("6 days ago")),
    ),
  )
}

@Preview(name = "Home · early data", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewHomeEarlyData() = FlowFinTheme {
  HomeScreen(
    state = HomeUiState.Content(
      currency = "Rs",
      totalWhole = "47,000",
      totalDecimal = ".00",
      allocated = "Rs 16,000",
      aside = HeroAside.Settling(dayLabel = UiText.Raw("Day 5")),
      realTotal = "Rs 47,000",
      realAccounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Bank", "", "40,000", ".00", "bank", "bank", isBudget = false),
        AccountCardUi(AccountId(Uuid.random()), "Cash", "", "7,000", ".00", "wallet", "cash", isBudget = false),
      ),
      budgetTotal = "Rs 16,000",
      budgetAccounts = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Food", "Bank", "16,000", ".00", "restaurant", "food", isBudget = true),
      ),
      pending = emptyList(),
      pendingTotal = 0,
      recent = RecentSection.Activity(
        listOf(
          RecentGroup(
            dateLabel = UiText.Raw("Today · 5 Jan"),
            rows = listOf(
              TxRowUi(TransactionId(Uuid.random()), UiText.Raw("Mess Lunch"), "Food", "−500", ".00", TransactionKind.Expense, "restaurant", "food"),
            ),
          ),
        ),
      ),
    ),
  )
}
