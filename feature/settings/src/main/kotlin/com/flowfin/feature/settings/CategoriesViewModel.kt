package com.flowfin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.flowfin.core.domain.repository.CategoryRepository
import com.flowfin.core.domain.repository.TransactionRepository
import com.flowfin.core.domain.usecase.CreateCategory
import com.flowfin.core.model.Category
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.model.CategoryUsage
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.monthShortLabel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Category management. Defaults are read-only by design — the model forbids
 * editing or archiving them — so the screen offers those actions on customs
 * only, rather than showing controls that would fail.
 */
class CategoriesViewModel(
  private val categories: CategoryRepository,
  transactions: TransactionRepository,
  private val createCategory: CreateCategory,
  private val clock: Clock,
  private val zone: TimeZone,
) : ViewModel() {

  private val today = clock.now().toLocalDateTime(zone).date

  /** Everything the user is driving; the lists come from the repositories. */
  private val local = MutableStateFlow(CategoriesUiState())

  private val effectChannel = Channel<CategoriesEffect>(Channel.BUFFERED)
  val effects = effectChannel.receiveAsFlow()

  val uiState: StateFlow<CategoriesUiState> = combine(
    local,
    categories.observeAll(),
    transactions.observeCategoryUsage(),
  ) { current, all, usage ->
    val inScope = all.filter { it.scope == current.scope }
    val (archived, active) = inScope.partition { it.isArchived }
    val (defaults, customs) = active.partition { it.isDefault }
    current.copy(
      defaults = defaults.map { it.toRow(usage) },
      customs = customs.map { it.toRow(usage) },
      archived = archived.map { it.toRow(usage) },
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CategoriesUiState())

  fun onSelectScope(scope: CategoryScope) = local.update { it.copy(scope = scope, showArchived = false) }

  fun onToggleArchived() = local.update { it.copy(showArchived = !it.showArchived) }

  fun onAdd() = local.update { it.copy(sheet = CategorySheet.Add, form = CategoryFormUi()) }

  fun onEdit(id: CategoryId) {
    val row = uiState.value.let { it.customs + it.archived }.firstOrNull { it.id == id } ?: return
    local.update {
      it.copy(
        sheet = CategorySheet.Edit(id),
        form = CategoryFormUi(
          name = row.name,
          iconKey = row.iconKey ?: DEFAULT_ICON_KEY,
          colorKey = row.colorKey ?: DEFAULT_COLOR_KEY,
        ),
      )
    }
  }

  fun onDismissSheet() = local.update { it.copy(sheet = null) }

  fun onNameChange(name: String) = local.update { it.copy(form = it.form.copy(name = name)) }

  fun onIconChange(key: String) = local.update { it.copy(form = it.form.copy(iconKey = key)) }

  fun onColorChange(key: String) = local.update { it.copy(form = it.form.copy(colorKey = key)) }

  fun onSave() {
    val state = local.value
    val form = state.form
    if (!form.canSave) return
    local.update { it.copy(form = it.form.copy(saving = true)) }

    viewModelScope.launch {
      val result = when (val sheet = state.sheet) {
        CategorySheet.Add -> createCategory(form.name, state.scope, form.iconKey, form.colorKey).map { }
        is CategorySheet.Edit -> {
          // Re-read for displayOrder: editing name/icon/colour must not silently
          // reshuffle the list.
          val existing = categories.getById(sheet.id)
          if (existing == null) {
            local.update { it.copy(form = it.form.copy(saving = false)) }
            effectChannel.send(CategoriesEffect.ShowMessage(UiText.Res(R.string.categories_error)))
            return@launch
          }
          categories.updateCustom(sheet.id, form.name.trim(), form.iconKey, form.colorKey, existing.displayOrder)
        }
        null -> return@launch
      }
      when (result) {
        is Either.Right -> local.update { it.copy(sheet = null, form = CategoryFormUi()) }
        is Either.Left -> {
          local.update { it.copy(form = it.form.copy(saving = false)) }
          effectChannel.send(CategoriesEffect.ShowMessage(UiText.Res(R.string.categories_error)))
        }
      }
    }
  }

  fun onArchive(id: CategoryId) = run(id) { categories.archive(it) }

  fun onUnarchive(id: CategoryId) = run(id) { categories.unarchive(it) }

  private fun run(id: CategoryId, block: suspend (CategoryId) -> Either<*, *>) {
    viewModelScope.launch {
      if (block(id) is Either.Left) {
        effectChannel.send(CategoriesEffect.ShowMessage(UiText.Res(R.string.categories_error)))
      } else {
        local.update { it.copy(sheet = null) }
      }
    }
  }

  private fun Category.toRow(usage: Map<CategoryId, CategoryUsage>): CategoryRowUi {
    val used = usage[id]
    return CategoryRowUi(
      id = id,
      name = name,
      iconKey = icon,
      colorKey = color,
      count = used?.transactionCount ?: 0,
      // Defaults show a lock instead — their provenance is that they shipped.
      meta = if (isDefault) null else customMeta(createdAt, used?.lastUsedAt),
      isDefault = isDefault,
    )
  }

  private fun customMeta(createdAt: Instant, lastUsedAt: Instant?): UiText {
    val created = dayLabel(createdAt.toLocalDateTime(zone).date)
    return if (lastUsedAt == null) {
      UiText.Res(R.string.categories_meta_unused, listOf(created))
    } else {
      UiText.Res(R.string.categories_meta_used, listOf(created, relativeLabel(lastUsedAt.toLocalDateTime(zone).date)))
    }
  }

  private fun dayLabel(date: LocalDate): UiText =
    UiText.Res(R.string.categories_day, listOf(date.dayOfMonth, UiText.Raw(monthShortLabel(date))))

  private fun relativeLabel(date: LocalDate): UiText {
    val days = today.toEpochDays() - date.toEpochDays()
    return if (days <= 0) UiText.Res(R.string.home_days_ago_today) else UiText.Plural(R.plurals.home_days_ago, days)
  }
}

private const val STOP_TIMEOUT_MS = 5_000L
