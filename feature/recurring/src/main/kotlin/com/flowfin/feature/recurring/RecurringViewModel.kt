package com.flowfin.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.domain.usecase.FireSchedule
import com.flowfin.core.domain.usecase.SkipSchedule
import com.flowfin.core.model.Category
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringSchedule
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Drives the Recurring tab. Active schedules split into pending (due now /
 * overdue) and upcoming (future, grouped by month); "now" is sampled once at
 * construction so the due/late labels stay stable for the screen's life.
 *
 * Read-only: firing / skipping / pausing / adding land in a follow-up slice.
 */
class RecurringViewModel(
  recurring: RecurringRepository,
  categories: CategoryRepository,
  private val fireSchedule: FireSchedule,
  private val skipSchedule: SkipSchedule,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val zone = TimeZone.currentSystemDefault()
  private val today = Clock.System.now().toLocalDateTime(zone).date

  private val effectChannel = Channel<RecurringEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  // Guards against a double-tap firing/skipping the same schedule twice before the
  // reactive list drops its card. Touched only on the main thread (Compose callbacks
  // + viewModelScope.Main), so a plain set is enough.
  private val inFlight = mutableSetOf<RecurringScheduleId>()

  val uiState: StateFlow<RecurringUiState> = combine(
    recurring.observeAll(),
    categories.observeAll(),
  ) { schedules, categoryList ->
    buildState(schedules, categoryList.associateBy(Category::id))
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), RecurringUiState.Loading)

  /** Record the firing and advance the due date — the row then leaves Pending on the
   *  next emission. [name] is only for the confirmation message. */
  fun markPaid(id: RecurringScheduleId, name: String) {
    if (!inFlight.add(id)) return
    viewModelScope.launch {
      try {
        val effect = when (fireSchedule(id)) {
          is Either.Right -> RecurringEffect.ShowMessage(UiText.Res(R.string.recurring_paid_confirm, listOf(name)))
          is Either.Left -> RecurringEffect.ShowMessage(UiText.Res(R.string.recurring_action_error))
        }
        effectChannel.send(effect)
      } finally {
        inFlight.remove(id)
      }
    }
  }

  /** Advance the due date without recording anything. */
  fun skip(id: RecurringScheduleId) {
    if (!inFlight.add(id)) return
    viewModelScope.launch {
      try {
        if (skipSchedule(id) is Either.Left) {
          effectChannel.send(RecurringEffect.ShowMessage(UiText.Res(R.string.recurring_action_error)))
        }
      } finally {
        inFlight.remove(id)
      }
    }
  }

  private fun buildState(schedules: List<RecurringSchedule>, categoriesById: Map<*, Category>): RecurringUiState {
    if (schedules.isEmpty()) return RecurringUiState.Empty

    val active = schedules.filter { it.isActive }.sortedBy { it.nextDueAt }
    if (active.isEmpty()) return RecurringUiState.AllPaused(pausedCount = schedules.size)
    val (pending, upcoming) = active.partition { it.dueDate() <= today }

    return RecurringUiState.Content(
      pendingCount = pending.size,
      activeCount = upcoming.size,
      pending = pending.map { it.toPendingUi(categoriesById) },
      upcoming = upcoming
        .groupBy { LocalDate(it.dueDate().year, it.dueDate().month, 1) }
        .map { (monthStart, rows) ->
          RecurringMonthGroup(monthLabel(monthStart), rows.map { it.toUpcomingUi(categoriesById) })
        },
    )
  }

  private fun RecurringSchedule.dueDate(): LocalDate = nextDueAt.toLocalDateTime(zone).date

  private fun RecurringSchedule.toPendingUi(categoriesById: Map<*, Category>): RecurringPendingUi {
    val daysLate = today.toEpochDays() - dueDate().toEpochDays()
    val (status, urgency) = if (daysLate <= 0) {
      UiText.Res(R.string.home_pending_due_today) to RecurringUrgency.Due
    } else {
      UiText.Plural(R.plurals.home_pending_days_late, daysLate) to RecurringUrgency.Late
    }
    val category = categoriesById[categoryId]
    return RecurringPendingUi(
      id = id,
      name = name,
      schedule = recurrence.scheduleLabel(),
      status = status,
      urgency = urgency,
      amountWhole = money.whole(amount),
      amountDecimal = money.fraction(amount),
      iconKey = category?.icon,
      colorKey = category?.color,
    )
  }

  private fun RecurringSchedule.toUpcomingUi(categoriesById: Map<*, Category>): RecurringUpcomingUi {
    val due = dueDate()
    val inDays = due.toEpochDays() - today.toEpochDays()
    val category = categoriesById[categoryId]
    return RecurringUpcomingUi(
      id = id,
      name = name,
      freq = recurrence.freqLabel(),
      due = UiText.Plural(R.plurals.recurring_due_in, inDays, listOf(due.dayOfMonth, UiText.Raw(monthShort(due.month)), inDays)),
      amountWhole = money.whole(amount),
      amountDecimal = money.fraction(amount),
      iconKey = category?.icon,
      colorKey = category?.color,
    )
  }

  private fun Recurrence.scheduleLabel(): UiText = when (this) {
    is Recurrence.Weekly -> UiText.Res(R.string.recurring_schedule_weekly, listOf(UiText.Raw(weekdayShort(dayOfWeek))))
    is Recurrence.Monthly -> UiText.Res(R.string.recurring_schedule_monthly, listOf(UiText.Raw(ordinal(dayOfMonth))))
    is Recurrence.Yearly -> UiText.Res(R.string.recurring_schedule_yearly, listOf(dayOfMonth, UiText.Raw(monthShort(Month(month)))))
  }

  private fun Recurrence.freqLabel(): UiText = UiText.Res(
    when (this) {
      is Recurrence.Weekly -> R.string.recurring_freq_weekly
      is Recurrence.Monthly -> R.string.recurring_freq_monthly
      is Recurrence.Yearly -> R.string.recurring_freq_yearly
    },
  )

  private fun monthLabel(monthStart: LocalDate): UiText =
    UiText.Res(R.string.recurring_month_label, listOf(UiText.Raw(monthFull(monthStart.month)), monthStart.year))
}

private const val STOP_TIMEOUT_MS = 5_000L

private fun weekdayShort(isoDayNumber: Int): String = titleCase3(DayOfWeek(isoDayNumber).name)
private fun monthShort(month: Month): String = titleCase3(month.name)
private fun monthFull(month: Month): String = month.name.lowercase().replaceFirstChar { it.uppercase() }
private fun titleCase3(name: String): String = name.take(3).lowercase().replaceFirstChar { it.uppercase() }

/** 1 → "1st", 22 → "22nd", 13 → "13th". */
private fun ordinal(n: Int): String {
  val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
    1 -> "st"
    2 -> "nd"
    3 -> "rd"
    else -> "th"
  }
  return "$n$suffix"
}
