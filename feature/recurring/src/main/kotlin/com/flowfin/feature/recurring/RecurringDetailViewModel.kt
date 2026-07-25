package com.flowfin.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.domain.repository.AccountRepository
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.RecurringRepository
import com.flowfin.core.model.Account
import com.flowfin.core.model.Category
import com.flowfin.core.model.Recurrence
import com.flowfin.core.model.RecurringSchedule
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.MoneyFormatter
import com.flowfin.core.ui.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Drives Recurring schedule detail: loads one schedule, joins its account and
 * category, and exposes Pause/Resume + Delete. Pause/Resume re-loads in place
 * so the screen reflects the new status without leaving; Delete dismisses.
 */
class RecurringDetailViewModel(
  private val scheduleId: RecurringScheduleId,
  private val recurring: RecurringRepository,
  private val accounts: AccountRepository,
  private val categories: CategoryRepository,
  private val money: MoneyFormatter,
) : ViewModel() {

  private val zone = TimeZone.currentSystemDefault()

  private val _uiState = MutableStateFlow<RecurringDetailUiState>(RecurringDetailUiState.Loading)
  val uiState: StateFlow<RecurringDetailUiState> = _uiState.asStateFlow()

  private val effectChannel = Channel<RecurringDetailEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  init {
    viewModelScope.launch { _uiState.value = load() }
  }

  private suspend fun load(): RecurringDetailUiState {
    val schedule = recurring.getById(scheduleId) ?: return RecurringDetailUiState.NotFound
    val account = (schedule.fromAccountId ?: schedule.toAccountId)?.let { accounts.getById(it) }
    val category = schedule.categoryId?.let { categories.getById(it) }
    return schedule.toContent(account, category)
  }

  fun togglePause() {
    val state = _uiState.value as? RecurringDetailUiState.Content ?: return
    viewModelScope.launch {
      val result = if (state.isActive) recurring.pause(scheduleId) else recurring.resume(scheduleId)
      if (result is Either.Left) {
        effectChannel.send(RecurringDetailEffect.ShowMessage(UiText.Res(R.string.recurring_action_error)))
      } else {
        _uiState.value = load()
      }
    }
  }

  fun delete() {
    viewModelScope.launch {
      val effect = when (recurring.delete(scheduleId)) {
        is Either.Right -> RecurringDetailEffect.Dismiss
        is Either.Left -> RecurringDetailEffect.ShowMessage(UiText.Res(R.string.recurring_action_error))
      }
      effectChannel.send(effect)
    }
  }

  private fun RecurringSchedule.toContent(account: Account?, category: Category?): RecurringDetailUiState.Content {
    val isIncome = toAccountId != null
    val due = nextDueAt.toLocalDateTime(zone).date
    return RecurringDetailUiState.Content(
      name = name,
      kindTitle = UiText.Res(if (isIncome) R.string.tx_detail_kind_income else R.string.tx_detail_kind_expense),
      currency = money.symbol,
      amountWhole = money.whole(amount),
      amountDecimal = money.fraction(amount),
      heroIconKey = category?.icon,
      heroColorKey = category?.color,
      isActive = isActive,
      status = if (isActive) {
        UiText.Res(R.string.recurring_detail_status_active)
      } else {
        val since = pausedAt?.toLocalDateTime(zone)?.date
        if (since != null) {
          UiText.Res(R.string.recurring_paused_since, listOf(since.dayOfMonth, UiText.Raw(monthShort(since.month))))
        } else {
          UiText.Res(R.string.recurring_paused_since_unknown)
        }
      },
      scheduleLabel = recurrence.scheduleLabel(),
      nextDue = if (isActive) {
        UiText.Res(R.string.recurring_detail_next_due, listOf(due.dayOfMonth, UiText.Raw(monthShort(due.month)), due.year))
      } else {
        null
      },
      account = account?.let {
        RecurringDetailNode(
          role = UiText.Res(if (isIncome) R.string.tx_detail_role_received_in else R.string.tx_detail_role_paid_from),
          name = UiText.Raw(it.name),
          iconKey = it.icon,
          colorKey = it.color,
        )
      },
      category = category?.let {
        RecurringDetailNode(
          role = UiText.Res(R.string.tx_detail_role_category),
          name = UiText.Raw(it.name),
          iconKey = it.icon,
          colorKey = it.color,
        )
      },
    )
  }

  private fun Recurrence.scheduleLabel(): UiText = when (this) {
    is Recurrence.Weekly -> UiText.Res(R.string.recurring_schedule_weekly, listOf(UiText.Raw(weekdayShort(dayOfWeek))))
    is Recurrence.Monthly -> UiText.Res(R.string.recurring_schedule_monthly, listOf(UiText.Raw(ordinal(dayOfMonth))))
    is Recurrence.Yearly -> UiText.Res(R.string.recurring_schedule_yearly, listOf(dayOfMonth, UiText.Raw(monthShort(Month(month)))))
  }
}
