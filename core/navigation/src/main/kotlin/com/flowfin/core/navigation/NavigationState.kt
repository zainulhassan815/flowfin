package com.flowfin.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/** Navigation state that survives config changes and process death. */
@Composable
fun rememberNavigationState(
  startKey: NavKey,
  topLevelKeys: Set<NavKey>,
): NavigationState {
  val topLevelStack = rememberNavBackStack(startKey)
  val subStacks = topLevelKeys.associateWith { key -> rememberNavBackStack(key) }
  return remember(startKey, topLevelKeys) {
    NavigationState(startKey, topLevelStack, subStacks)
  }
}

/**
 * Holds one top-level stack (which tab is current) plus a per-tab sub-stack, so
 * each tab keeps its own back stack across switches.
 */
class NavigationState(
  val startKey: NavKey,
  val topLevelStack: NavBackStack<NavKey>,
  val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
  val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }

  val topLevelKeys get() = subStacks.keys

  val currentSubStack: NavBackStack<NavKey>
    get() = subStacks[currentTopLevelKey] ?: error("No sub stack for $currentTopLevelKey")

  val currentKey: NavKey by derivedStateOf { currentSubStack.last() }
}

/** Flatten the current tab's sub-stack into decorated [NavEntry]s for [androidx.navigation3.ui.NavDisplay]. */
@Composable
fun NavigationState.toEntries(
  entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
  val decoratedEntries = subStacks.mapValues { (_, stack) ->
    rememberDecoratedNavEntries(
      backStack = stack,
      entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
      entryProvider = entryProvider,
    )
  }
  return topLevelStack.flatMap { decoratedEntries[it] ?: emptyList() }.toMutableStateList()
}
