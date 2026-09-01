package com.flowfin.feature.accounts

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.BudgetProgress
import com.flowfin.core.designsystem.component.FlowFinAccountCard
import com.flowfin.core.designsystem.component.FlowFinEmptyState
import com.flowfin.core.designsystem.component.FlowFinIconButton
import com.flowfin.core.designsystem.component.FlowFinSectionEmptyHint
import com.flowfin.core.designsystem.component.FlowFinTextButton
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.AccountId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.AccountCardUi
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon
import kotlin.uuid.Uuid

private val HORIZONTAL = 24.dp

@Composable
fun AccountsListScreen(
  state: AccountsListUiState,
  modifier: Modifier = Modifier,
  onAddAccount: () -> Unit = {},
  onAccountClick: (AccountId) -> Unit = {},
  onSetBudget: () -> Unit = {},
) {
  Column(modifier.fillMaxSize().background(FlowFinTheme.colors.bg)) {
    AccountsTopBar(onAddAccount = onAddAccount)
    when (state) {
      AccountsListUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth())
      AccountsListUiState.Empty -> Box(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = HORIZONTAL),
        contentAlignment = Alignment.Center,
      ) {
        FlowFinEmptyState(
          eyebrow = stringResource(R.string.accounts_empty_eyebrow),
          title = stringResource(R.string.accounts_empty_title),
          body = stringResource(R.string.accounts_empty_body),
          actionLabel = stringResource(R.string.accounts_empty_action),
          onAction = onAddAccount,
        )
      }
      is AccountsListUiState.Content -> LazyColumn(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(bottom = 96.dp),
      ) {
        item(key = "hero") { Hero(state) }
        accountSection(R.string.accounts_section_real, state.realTotal, state.real, onAccountClick)
        budgetSection(state, onAccountClick, onSetBudget)
      }
    }
  }
}

@Composable
private fun AccountsTopBar(onAddAccount: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(start = HORIZONTAL, end = 14.dp, top = 12.dp, bottom = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(R.string.accounts_title),
      style = FlowFinTheme.typography.h2.copy(fontSize = 26.sp),
      color = FlowFinTheme.colors.text,
    )
    Spacer(Modifier.weight(1f))
    FlowFinIconButton(
      onClick = onAddAccount,
      icon = FlowFinIcons.Add,
      contentDescription = stringResource(R.string.accounts_action_add),
    )
  }
}

@Composable
private fun Hero(state: AccountsListUiState.Content) {
  val palette = FlowFinTheme.colors
  Column(Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL, vertical = 16.dp)) {
    Text(
      text = stringResource(R.string.accounts_total_label).uppercase(),
      style = FlowFinTheme.typography.label,
      color = palette.textMute,
    )
    Row(modifier = Modifier.padding(top = 10.dp)) {
      Text(
        text = state.currency,
        modifier = Modifier.alignByBaseline(),
        style = FlowFinTheme.typography.h2.copy(fontSize = 22.sp, fontWeight = FontWeight.Light),
        color = palette.textMute,
      )
      Text(
        text = state.totalWhole,
        modifier = Modifier.alignByBaseline().padding(start = 8.dp),
        style = FlowFinTheme.typography.hero.copy(fontSize = 44.sp, lineHeight = 44.sp),
        color = palette.text,
      )
      Text(
        text = state.totalDecimal,
        modifier = Modifier.alignByBaseline().padding(start = 4.dp),
        style = FlowFinTheme.typography.monoNum.copy(fontSize = 20.sp, fontWeight = FontWeight.Light),
        color = palette.textMute,
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      BreakdownItem(stringResource(R.string.accounts_breakdown_real), state.realTotal)
      BreakdownDivider()
      BreakdownItem(stringResource(R.string.accounts_breakdown_budget), state.budgetTotal)
      BreakdownDivider()
      BreakdownItem(stringResource(R.string.accounts_breakdown_count), state.accountCount.toString())
    }
  }
}

@Composable
private fun BreakdownItem(label: String, value: String) {
  val palette = FlowFinTheme.colors
  Column {
    Text(label.uppercase(), style = FlowFinTheme.typography.caption.copy(fontSize = 10.sp, letterSpacing = 0.16.em), color = palette.textSoft)
    Text(value, modifier = Modifier.padding(top = 3.dp), style = FlowFinTheme.typography.caption.copy(fontSize = 13.sp), color = palette.text)
  }
}

@Composable
private fun BreakdownDivider() {
  Box(Modifier.width(1.dp).height(22.dp).background(FlowFinTheme.colors.border))
}

private fun LazyListScope.accountSection(
  @StringRes titleRes: Int,
  total: String,
  cards: List<AccountCardUi>,
  onAccountClick: (AccountId) -> Unit,
) {
  if (cards.isEmpty()) return
  item(key = "header-$titleRes") { SectionHeader(stringResource(titleRes), total) }
  items(cards, key = { "acct-${it.id.value}" }) { card -> AccountCardItem(card, onAccountClick) }
}

private fun LazyListScope.budgetSection(
  state: AccountsListUiState.Content,
  onAccountClick: (AccountId) -> Unit,
  onSetBudget: () -> Unit,
) {
  val empty = state.budgets.isEmpty()
  item(key = "header-budget") {
    SectionHeader(
      title = stringResource(R.string.accounts_section_budget),
      total = if (empty) stringResource(R.string.accounts_budgets_total_empty) else state.budgetTotal,
      totalFaint = empty,
    )
  }
  if (empty) {
    item(key = "budgets-empty") {
      Box(Modifier.padding(horizontal = HORIZONTAL, vertical = 4.dp)) {
        FlowFinSectionEmptyHint(
          eyebrow = stringResource(R.string.accounts_budgets_empty_eyebrow),
          title = stringResource(R.string.accounts_budgets_empty_title),
          body = stringResource(R.string.accounts_budgets_empty_body),
          actionLabel = stringResource(R.string.accounts_budgets_empty_action),
          onAction = onSetBudget,
        )
      }
    }
  } else {
    items(state.budgets, key = { "acct-${it.id.value}" }) { card -> AccountCardItem(card, onAccountClick) }
    // Once the section is populated its empty-state link is gone, and the header's
    // "+" makes real accounts — so without this there is no route to a second budget.
    item(key = "budgets-add") {
      Box(Modifier.padding(horizontal = HORIZONTAL, vertical = 6.dp)) {
        FlowFinTextButton(
          text = stringResource(R.string.accounts_budgets_add),
          onClick = onSetBudget,
        )
      }
    }
  }
}

@Composable
private fun SectionHeader(title: String, total: String, totalFaint: Boolean = false) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = HORIZONTAL)
      .padding(top = 20.dp, bottom = 10.dp),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = title.uppercase(),
      style = FlowFinTheme.typography.caption.copy(letterSpacing = 0.24.em),
      color = palette.textMute,
    )
    Text(
      text = total,
      style = FlowFinTheme.typography.caption.copy(fontSize = 11.sp, letterSpacing = 0.05.em),
      color = if (totalFaint) palette.textFaint else palette.text,
    )
  }
}

@Composable
private fun AccountCardItem(card: AccountCardUi, onAccountClick: (AccountId) -> Unit) {
  Box(Modifier.padding(horizontal = HORIZONTAL, vertical = 4.dp)) {
    FlowFinAccountCard(
      icon = categoryIcon(card.iconKey),
      name = card.name,
      meta = card.meta,
      balance = card.balanceWhole,
      decimal = card.balanceDecimal,
      tint = card.colorKey?.let { categoryColor(it) },
      dashedIcon = card.isBudget,
      progress = card.progress,
      onClick = { onAccountClick(card.id) },
    )
  }
}

@Preview(name = "Accounts", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAccounts() = FlowFinTheme {
  AccountsListScreen(
    state = AccountsListUiState.Content(
      currency = "Rs",
      totalWhole = "47,000",
      totalDecimal = ".00",
      realTotal = "Rs 47,000",
      budgetTotal = "Rs 16,000",
      accountCount = 4,
      real = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Bank", "", "40,000", ".00", "bank", "bank", isBudget = false),
        AccountCardUi(AccountId(Uuid.random()), "Cash", "", "7,000", ".00", "wallet", "cash", isBudget = false),
      ),
      budgets = listOf(
        AccountCardUi(
          AccountId(Uuid.random()), "Food", "Bank", "9,500", ".00", "restaurant", "food", isBudget = true,
          progress = BudgetProgress(spent = "Rs 6,500", total = "Rs 16,000", fraction = 0.41f),
        ),
      ),
    ),
  )
}

@Preview(name = "Accounts · no budgets", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAccountsNoBudgets() = FlowFinTheme {
  AccountsListScreen(
    state = AccountsListUiState.Content(
      currency = "Rs",
      totalWhole = "47,000",
      totalDecimal = ".00",
      realTotal = "Rs 47,000",
      budgetTotal = "Rs 0",
      accountCount = 2,
      real = listOf(
        AccountCardUi(AccountId(Uuid.random()), "Bank", "", "40,000", ".00", "bank", "bank", isBudget = false),
        AccountCardUi(AccountId(Uuid.random()), "Cash", "", "7,000", ".00", "wallet", "cash", isBudget = false),
      ),
      budgets = emptyList(),
    ),
  )
}

@Preview(name = "Accounts · empty", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewAccountsEmpty() = FlowFinTheme {
  AccountsListScreen(state = AccountsListUiState.Empty)
}
