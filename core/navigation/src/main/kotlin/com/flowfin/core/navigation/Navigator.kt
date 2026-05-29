package com.flowfin.core.navigation

import androidx.navigation3.runtime.NavKey

/** Drives forward/back navigation by mutating [NavigationState]. */
class Navigator(val state: NavigationState) {

  fun navigate(key: NavKey) {
    when (key) {
      state.currentTopLevelKey -> clearSubStack() // re-tap current tab → pop to its root
      in state.topLevelKeys -> goToTopLevel(key)
      else -> goToKey(key)
    }
  }

  fun goBack() {
    when (state.currentKey) {
      state.startKey -> error("Cannot go back from the start route")
      state.currentTopLevelKey -> state.topLevelStack.removeLastOrNull() // base of a tab → previous tab
      else -> state.currentSubStack.removeLastOrNull()
    }
  }

  private fun goToKey(key: NavKey) {
    state.currentSubStack.apply {
      remove(key) // single-top: move to the end if already present
      add(key)
    }
  }

  private fun goToTopLevel(key: NavKey) {
    state.topLevelStack.apply {
      if (key == state.startKey) clear() else remove(key)
      add(key)
    }
  }

  private fun clearSubStack() {
    state.currentSubStack.run { if (size > 1) subList(1, size).clear() }
  }
}
