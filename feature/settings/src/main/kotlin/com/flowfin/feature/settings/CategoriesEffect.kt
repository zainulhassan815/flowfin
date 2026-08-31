package com.flowfin.feature.settings

import com.flowfin.core.ui.UiText

/** One-shot effects from [CategoriesViewModel], consumed in the entry. */
sealed interface CategoriesEffect {
  data class ShowMessage(val text: UiText) : CategoriesEffect
}
