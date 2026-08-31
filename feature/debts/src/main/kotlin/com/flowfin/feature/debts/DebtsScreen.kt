package com.flowfin.feature.debts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinHeroAmount
import com.flowfin.core.designsystem.component.FlowFinPersonAvatar
import com.flowfin.core.designsystem.component.FlowFinProgressBar
import com.flowfin.core.designsystem.component.FlowFinSegmentedControl
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import kotlin.uuid.Uuid

private val HORIZONTAL = 24.dp

@Composable
fun DebtsScreen(
  state: DebtsUiState,
  modifier: Modifier = Modifier,
  onDebtClick: (DebtId) -> Unit = {},
) {
  Column(modifier.fillMaxSize().background(FlowFinTheme.colors.bg)) {
    Header(state)
    when (state) {
      DebtsUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth())
      DebtsUiState.Empty -> Notice(
        eyebrow = stringResource(R.string.debts_empty_eyebrow),
        title = stringResource(R.string.debts_empty_title),
        body = stringResource(R.string.debts_empty_body),
      )
      is DebtsUiState.Content -> DebtsTabs(state, onDebtClick, Modifier.weight(1f))
    }
  }
}

@Composable
private fun Header(state: DebtsUiState) {
  val palette = FlowFinTheme.colors
  Column(
    Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(top = 12.dp),
  ) {
    Text(
      text = stringResource(R.string.debts_title),
      modifier = Modifier.padding(horizontal = HORIZONTAL),
      style = FlowFinTheme.typography.h2.copy(fontSize = 26.sp),
      color = palette.text,
    )
    if (state is DebtsUiState.Content) HeroNetPosition(state)
  }
}

@Composable
private fun HeroNetPosition(state: DebtsUiState.Content) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.debts_net_position_label).uppercase(),
      style = FlowFinTheme.typography.caption,
      color = palette.textFaint,
    )
    if (state.allSettled) {
      Row(
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(imageVector = FlowFinIcons.Check, contentDescription = null, tint = palette.positive)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.debts_all_clear), style = FlowFinTheme.typography.h2.copy(fontSize = 22.sp), color = palette.text)
      }
    } else {
      val negative = state.netPositionWhole.startsWith("-")
      FlowFinHeroAmount(
        whole = state.netPositionWhole,
        decimal = state.netPositionDecimal,
        tint = if (negative) palette.warning else palette.positive,
      )
    }
  }
}

@Composable
private fun DebtsTabs(
  state: DebtsUiState.Content,
  onDebtClick: (DebtId) -> Unit,
  modifier: Modifier = Modifier,
) {
  var direction by remember { mutableStateOf(DebtDirection.I_OWE) }
  val iOweLabel = "${stringResource(R.string.debts_tab_i_owe)} (${state.iOwe.active.size})"
  val oweMeLabel = "${stringResource(R.string.debts_tab_owe_me)} (${state.oweMe.active.size})"

  Column(modifier) {
    FlowFinSegmentedControl(
      options = listOf(DebtDirection.I_OWE, DebtDirection.OWED_TO_ME),
      selected = direction,
      onSelect = { direction = it },
      modifier = Modifier.padding(horizontal = HORIZONTAL, vertical = 8.dp),
      label = { if (it == DebtDirection.I_OWE) iOweLabel else oweMeLabel },
    )

    val tab = if (direction == DebtDirection.I_OWE) state.iOwe else state.oweMe
    val amountLabelRes = if (direction == DebtDirection.I_OWE) R.string.debts_amount_label_i_owe else R.string.debts_amount_label_owe_me
    var showSettled by remember(direction) { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(start = HORIZONTAL, end = HORIZONTAL, bottom = 96.dp)) {
      items(tab.active, key = { it.id.value.toString() }) { DebtCard(it, stringResource(amountLabelRes), onDebtClick) }
      if (tab.settled.isNotEmpty()) {
        item(key = "settled-toggle") { SettledToggle(tab.settled.size) { showSettled = !showSettled } }
        if (showSettled) {
          items(tab.settled, key = { it.id.value.toString() }) { DebtCard(it, stringResource(amountLabelRes), onDebtClick) }
        }
      }
    }
  }
}

@Composable
private fun SettledToggle(count: Int, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp)
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = pluralStringResource(R.plurals.debts_settled_toggle, count, count),
      style = FlowFinTheme.typography.caption,
      color = palette.textSoft,
    )
    Spacer(Modifier.width(6.dp))
    Icon(
      imageVector = FlowFinIcons.ChevronRight,
      contentDescription = null,
      modifier = Modifier.size(14.dp),
      tint = palette.textFaint,
    )
  }
}

@Composable
private fun DebtCard(row: DebtCardUi, amountLabel: String, onClick: (DebtId) -> Unit) {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(palette.surface)
      .clickable { onClick(row.id) }
      .padding(14.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      FlowFinPersonAvatar(
        initial = row.personName.take(1).uppercase(),
        tint = palette.avatars.byIndex(row.avatarTintIndex),
      )
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        Text(row.personName, style = FlowFinTheme.typography.bodyLg, color = palette.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (row.reason != null) {
          Text(
            text = "“${row.reason}”",
            style = FlowFinTheme.typography.body.copy(fontSize = 13.sp, fontStyle = FontStyle.Italic),
            color = palette.textSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Spacer(Modifier.width(8.dp))
      Column(horizontalAlignment = Alignment.End) {
        Amount(row.amountWhole, row.amountDecimal)
        Text(amountLabel, style = FlowFinTheme.typography.caption, color = palette.textFaint)
      }
    }
    FlowFinProgressBar(
      progress = row.progress,
      modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
      color = palette.accent,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stringResource(R.string.debts_card_paid, "${row.paidWhole}${row.paidDecimal}", "${row.amountWhole}${row.amountDecimal}"),
        style = FlowFinTheme.typography.caption,
        color = palette.textSoft,
      )
      Text(row.dateLabel.asString(), style = FlowFinTheme.typography.caption, color = palette.textFaint)
    }
  }
}

@Composable
private fun Amount(whole: String, decimal: String) {
  val palette = FlowFinTheme.colors
  Row(verticalAlignment = Alignment.Bottom) {
    Text(whole, style = FlowFinTheme.typography.monoNum.copy(fontSize = 16.sp), color = palette.text)
    Text(decimal, style = FlowFinTheme.typography.monoNum.copy(fontSize = 12.sp), color = palette.textFaint)
  }
}

/** Centered, CTA-less informational state — the whole-screen empty state. */
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
      textAlign = TextAlign.Center,
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

@Preview(name = "Debts · populated", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewDebts() = FlowFinTheme {
  fun fakeId() = DebtId(Uuid.random())
  DebtsScreen(
    state = DebtsUiState.Content(
      netPositionWhole = "+11,500",
      netPositionDecimal = ".00",
      allSettled = false,
      iOwe = DebtsTabUi(
        active = listOf(
          DebtCardUi(fakeId(), "Ahmed", 3, "Borrowed for rent", "3,000", ".00", "2,000", ".00", 0.4f, UiText.Raw("20 May · 6 days ago")),
          DebtCardUi(fakeId(), "Hassan", 2, null, "2,500", ".00", "0", ".00", 0f, UiText.Raw("1 May · 25 days ago")),
        ),
        settled = emptyList(),
      ),
      oweMe = DebtsTabUi(
        active = listOf(
          DebtCardUi(fakeId(), "Ali", 4, "Lunch", "2,000", ".00", "1,000", ".00", 0.33f, UiText.Raw("10 May · 16 days ago")),
        ),
        settled = listOf(
          DebtCardUi(fakeId(), "Imran", 5, null, "0", ".00", "10,000", ".00", 1f, UiText.Raw("1 Apr · 55 days ago")),
        ),
      ),
    ),
  )
}
