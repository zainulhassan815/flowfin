package com.flowfin.feature.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowfin.core.domain.repository.DebtRepository
import com.flowfin.core.domain.repository.PersonRepository
import com.flowfin.core.model.DebtDirection
import com.flowfin.core.model.DebtStatus
import com.flowfin.core.model.DebtWithRemaining
import com.flowfin.core.model.Money
import com.flowfin.core.model.Person
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.monthShortLabel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Drives the Debts tab. Active debts split by [DebtDirection]; within each
 * direction, active debts lead and settled ones sit behind the tab's own
 * disclosure. "Now" is sampled once at construction so relative date labels
 * stay stable for the screen's life.
 */
class DebtsViewModel(
  debts: DebtRepository,
  persons: PersonRepository,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val zone = TimeZone.currentSystemDefault()
  private val today = Clock.System.now().toLocalDateTime(zone).date

  val uiState: StateFlow<DebtsUiState> = combine(
    debts.observeAll(),
    persons.observeActive(),
  ) { allDebts, personList ->
    buildState(allDebts, personList.associateBy { it.id })
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DebtsUiState.Loading)

  private fun buildState(allDebts: List<DebtWithRemaining>, personsById: Map<*, Person>): DebtsUiState {
    if (allDebts.isEmpty()) return DebtsUiState.Empty

    val netPosition = allDebts
      .filter { it.debt.status == DebtStatus.ACTIVE }
      .fold(Money.ZERO) { total, row ->
        when (row.debt.direction) {
          DebtDirection.OWED_TO_ME -> total + row.remaining
          DebtDirection.I_OWE -> total - row.remaining
        }
      }

    return DebtsUiState.Content(
      netPositionWhole = (if (netPosition.isNegative) "" else "+") + money.whole(netPosition),
      netPositionDecimal = money.fraction(netPosition),
      allSettled = allDebts.all { it.debt.status == DebtStatus.SETTLED },
      iOwe = tabFor(allDebts, DebtDirection.I_OWE, personsById),
      oweMe = tabFor(allDebts, DebtDirection.OWED_TO_ME, personsById),
    )
  }

  private fun tabFor(allDebts: List<DebtWithRemaining>, direction: DebtDirection, personsById: Map<*, Person>): DebtsTabUi {
    val (settled, active) = allDebts
      .filter { it.debt.direction == direction }
      .partition { it.debt.status == DebtStatus.SETTLED }
    return DebtsTabUi(
      active = active.map { it.toCardUi(personsById) },
      settled = settled.map { it.toCardUi(personsById) },
    )
  }

  private fun DebtWithRemaining.toCardUi(personsById: Map<*, Person>): DebtCardUi {
    val person = personsById[debt.personId]
    val original = debt.originalAmount.minorUnits.toFloat()
    val date = originRecordedAt.toLocalDateTime(zone).date
    return DebtCardUi(
      id = debt.id,
      personName = person?.name.orEmpty(),
      avatarTintIndex = person?.avatarTintIndex ?: 1,
      reason = debt.reason,
      amountWhole = money.whole(remaining),
      amountDecimal = money.fraction(remaining),
      paidWhole = money.whole(paid),
      paidDecimal = money.fraction(paid),
      progress = if (original > 0f) (paid.minorUnits.toFloat() / original).coerceIn(0f, 1f) else 0f,
      dateLabel = dateLabel(date),
    )
  }

  private fun dateLabel(date: LocalDate): UiText {
    val days = today.toEpochDays() - date.toEpochDays()
    val relative = if (days <= 0) UiText.Res(R.string.home_days_ago_today) else UiText.Plural(R.plurals.home_days_ago, days)
    return UiText.Res(R.string.debts_card_date, listOf(date.dayOfMonth, UiText.Raw(monthShortLabel(date)), relative))
  }
}

private const val STOP_TIMEOUT_MS = 5_000L
