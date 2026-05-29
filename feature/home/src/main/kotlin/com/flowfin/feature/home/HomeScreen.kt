package com.flowfin.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowfin.core.designsystem.component.FlowFinFab
import com.flowfin.core.designsystem.component.FlowFinNavBar
import com.flowfin.core.designsystem.component.FlowFinNavBarItem
import com.flowfin.core.designsystem.theme.FlowFinTheme
import org.koin.compose.viewmodel.koinViewModel

/** The five bottom-nav destinations. */
enum class HomeTab { Home, Accounts, Recur, Debts, Reports }

/** Stateful entry point: pulls [HomeUiState] from the ViewModel and renders it. */
@Composable
fun HomeRoute(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = koinViewModel(),
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  HomeScreen(state = state, modifier = modifier)
}

@Composable
fun HomeScreen(
  state: HomeUiState,
  modifier: Modifier = Modifier,
  onAddTransaction: () -> Unit = {},
  onSelectTab: (HomeTab) -> Unit = {},
) {
  val palette = FlowFinTheme.colors

  Scaffold(
    modifier = modifier,
    containerColor = palette.bg,
    bottomBar = { HomeNavBar(selected = HomeTab.Home, onSelect = onSelectTab) },
    floatingActionButton = {
      FlowFinFab(
        onClick = onAddTransaction,
        icon = Icons.Rounded.Add,
        contentDescription = "Add transaction",
      )
    },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState()),
    ) {
      Hero(state)
      RecentActivity(state.recent)
    }
  }
}

/** Interim plain-text feed; replaced by hydrated transaction rows in the Home design pass. */
@Composable
private fun RecentActivity(lines: List<String>) {
  if (lines.isEmpty()) return
  val palette = FlowFinTheme.colors

  Column(
    modifier = Modifier.padding(horizontal = 24.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = "Recent".uppercase(),
      style = FlowFinTheme.typography.label,
      color = palette.textMute,
    )
    lines.forEach { line ->
      Text(text = line, style = FlowFinTheme.typography.body, color = palette.text)
    }
  }
}

@Composable
private fun HomeNavBar(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
  FlowFinNavBar {
    FlowFinNavBarItem(Icons.Rounded.Home, "Home", selected == HomeTab.Home, onClick = { onSelect(HomeTab.Home) })
    FlowFinNavBarItem(Icons.Rounded.AccountBalanceWallet, "Accounts", selected == HomeTab.Accounts, onClick = { onSelect(HomeTab.Accounts) })
    FlowFinNavBarItem(Icons.Rounded.Autorenew, "Recur", selected == HomeTab.Recur, onClick = { onSelect(HomeTab.Recur) })
    FlowFinNavBarItem(Icons.Rounded.People, "Debts", selected == HomeTab.Debts, onClick = { onSelect(HomeTab.Debts) })
    FlowFinNavBarItem(Icons.Rounded.BarChart, "Reports", selected == HomeTab.Reports, onClick = { onSelect(HomeTab.Reports) })
  }
}

@Composable
private fun Hero(state: HomeUiState) {
  val palette = FlowFinTheme.colors

  Column(modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier = Modifier
          .width(14.dp)
          .height(1.dp)
          .background(palette.textSoft),
      )
      Text(
        text = "Available balance".uppercase(),
        style = FlowFinTheme.typography.label,
        color = palette.textMute,
      )
    }

    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(
        text = state.currency,
        modifier = Modifier.alignByBaseline(),
        style = FlowFinTheme.typography.h2.copy(fontSize = 24.sp, fontWeight = FontWeight.Light),
        color = palette.textMute,
      )
      Text(
        text = state.balanceWhole,
        modifier = Modifier.alignByBaseline(),
        style = FlowFinTheme.typography.hero,
        color = palette.text,
      )
      Text(
        text = state.balanceDecimal,
        modifier = Modifier.alignByBaseline(),
        style = FlowFinTheme.typography.hero.copy(fontSize = 24.sp, letterSpacing = (-0.02).em),
        color = palette.textMute,
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
        .drawBehind {
          drawLine(
            color = palette.borderStrong,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())),
          )
        }
        .padding(top = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val metaStyle = FlowFinTheme.typography.monoNum.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.05.em,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Allocated", style = metaStyle, color = palette.textMute)
        Text(state.allocated, style = metaStyle, color = palette.text)
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Rounded.NorthEast,
            contentDescription = null,
            tint = palette.positive,
            modifier = Modifier.size(9.dp),
          )
          Text(state.trend, style = metaStyle, color = palette.positive)
        }
        Text("this month", style = metaStyle, color = palette.textMute)
      }
    }
  }
}

@Preview(name = "Home", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun PreviewHomeScreen() = FlowFinTheme {
  HomeScreen(
    state = HomeUiState(
      currency = "Rs",
      balanceWhole = "47,000",
      balanceDecimal = ".00",
      allocated = "Rs 16,000",
      trend = "+12%",
    ),
  )
}
