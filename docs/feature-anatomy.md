# FlowFin — feature anatomy

How a `:feature:*` module is built, end to end, and the patterns every feature should follow. Home is the reference implementation (read-only screen); Add-Transaction is the reference for a write flow. Use this as the base shape when starting a new feature; refine it as the app grows.

## The layers a feature touches

A feature is the top of a one-way stack. It reads domain repositories, maps their models into display models, and renders them with design-system components. It never reaches the database or holds an Android `Context` outside of `@Composable` code.

```
:feature:x            screen, sections, UiState, ViewModel, di, navigation entry
:core:ui              display models, MoneyFormatter, UiText, category icon/color resolvers
:core:designsystem    theme tokens + FlowFin* components (model-agnostic)
:core:domain          repository interfaces, use cases, models, typed errors
:core:navigation      route ADT + Navigator
:core:resources       strings.xml
```

The dependency edges are wired once, in `AndroidFeatureConventionPlugin` (`build-logic/`): every `:feature:*` automatically gets `:core:{designsystem, domain, ui, navigation, resources}` plus lifecycle-compose and navigation3-runtime. A feature's own `build.gradle.kts` only declares the plugin and namespace, adding dependencies (e.g. `kotlinx.datetime`) only when it needs something beyond the baseline.

```kotlin
plugins { alias(libs.plugins.flowfin.android.feature) }
android { namespace = "com.flowfin.feature.x" }
```

## File layout

One package per feature, flat, with a small `navigation/` and `di/` alongside. Home:

```
feature/home/src/main/kotlin/com/flowfin/feature/home/
  HomeScreen.kt        the stateless screen + top bar + previews
  HomeSections.kt      LazyListScope section builders + private row composables
  HomeUiState.kt       the sealed UiState and its supporting display types
  HomeViewModel.kt     repo → state mapping
  di/HomeModule.kt     viewModelOf(::HomeViewModel)
  navigation/HomeEntry.kt  EntryProviderScope.homeEntry(navigator)
```

Split the UI when a screen grows: `HomeScreen.kt` owns the top-level composable, state routing, and previews; `HomeSections.kt` owns the per-section builders. Keep each row/section composable `private` to its file — only the entry-point screen is public.

## State modeling

Model UI state as a sealed interface, with `Loading` plus **data-derived** states — not a flat data class with nullable everything. The shape of the state should make illegal combinations unrepresentable.

```kotlin
sealed interface HomeUiState {
  data object Loading : HomeUiState
  data class Empty(val currency: String) : HomeUiState
  data class Content(/* fully-formed, display-ready fields */) : HomeUiState
}
```

Conventions that hold across features:

- **No error state on a read screen.** A local SQLite read has no recoverable domain failure; a genuine failure is a crash, not a per-screen "something went wrong". States are `Loading` + the data-derived states. (See [`decisions.md`](decisions.md#errors).)
- **Nest empty/variant states where they belong.** Home's whole-screen empty (no accounts) is a top-level `Empty`; the Recent section's own empties (`NoEntries`, `Quiet`) live inside `Content.recent` as a nested sealed interface; an empty Pending list just drops the section. Push each "emptiness" to the smallest scope that owns it.
- **Content fields are display-ready.** `Content` holds formatted strings (`totalWhole`, `allocated`), resolved enums (the design-system `TransactionKind`, not the domain one), and `UiText` for anything a `@Composable` must resolve. No domain models leak into UI state — the ViewModel does the mapping.

## The ViewModel

Combine the repository streams, map to state in one pure function, and expose a single `StateFlow`.

```kotlin
val uiState: StateFlow<HomeUiState> = combine(
  accounts.observeBalances(),
  transactions.recentFeed(RECENT_LIMIT),
  categories.observeAll(),
  recurring.observePending(now),
  transactions.observeNetChange(monthStart, monthEnd),
) { balances, recent, categoryList, pending, net ->
  buildState(balances, recent, categoryList, pending, net)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Loading)
```

Patterns to keep:

- **Constructor-inject repositories and `MoneyFormatter`; never a `Context`.** Koin provides them. Read methods are plain `Flow` / suspend `T?`; writes go through a use case and return `Either`.
- **`stateIn(viewModelScope, WhileSubscribed(5_000), Loading)`.** The 5s stop timeout keeps the upstream alive across config changes without leaking when the screen is gone. The initial value is `Loading`.
- **`buildState` is a pure mapper.** It takes the combined snapshots and returns a state; no side effects, no I/O. Empty-vs-content branching happens here (`if (balances.isEmpty()) return Empty(...)`).
- **Sample "now" once, at construction.** `private val now = clock.now()` and the derived `today` / `monthStart` / `weekStart` are computed once so date labels and "pending" thresholds are stable for the ViewModel's life. Inject `Clock` (and `TimeZone` where needed) rather than calling `Clock.System` — it keeps the VM testable.
- **Mapping helpers are private extensions** (`AccountBalance.toCardUi`, `Transaction.toRowUi`). They translate domain → display model and own the domain-kind → UI-kind switch, the icon/color key defaults, and the +/− sign convention.
- **Tunables are named `private const` with a comment explaining the threshold** (`PENDING_LIMIT`, `SETTLING_DAYS`, `RECENT_LIMIT`). Compute on read — no cached/stored aggregates (see [no premature optimization](decisions.md#quality-bar)).

## The screen (UI)

The screen is **stateless**: it takes `state` plus callbacks, and renders. It holds no ViewModel reference — that lives in the navigation entry.

```kotlin
@Composable
fun HomeScreen(
  state: HomeUiState,
  modifier: Modifier = Modifier,
  onTransactionClick: (TransactionId) -> Unit = {},
  onAccountClick: (AccountId) -> Unit = {},
  // … one lambda per user intent, defaulting to {}
) { … }
```

- **Route on the sealed state with an exhaustive `when`.** Each arm renders its own subtree (`Loading` → a blank weighted box; `Empty` → hero + empty-state; `Content` → the list).
- **Lists use `LazyColumn`, and sections are `LazyListScope` extension functions** (`fun LazyListScope.accountsSection(...)`). This keeps each section independently testable/movable and lets the whole screen scroll as one list. Give every item a stable `key` (e.g. `"acct-${row.id.value}"`) and a `contentType` so Compose can recycle correctly.
- **Compose from the design system; never rebuild M3 mechanics.** Reach for `FlowFinTileIcon`, `FlowFinTransactionRow`, `FlowFinEmptyState`, `FlowFinIconButton`, etc. Pull all color/typography from `FlowFinTheme.colors` / `FlowFinTheme.typography` — no hardcoded hex or `sp` magic outside small, local layout tweaks. Icons come from `FlowFinIcons`, resolved from string keys via `categoryIcon()` / `categoryColor()`.
- **Resolve `UiText` at the point of use** with `.asString()`; resolve static copy with `stringResource(R.string.…)`. See [`localization.md`](localization.md).
- **Co-locate `@Preview`s for every meaningful state.** Home previews Content, Empty, no-entries, quiet-week, and early-data — each wrapped in `FlowFinTheme`, with sample data inline (fixtures, not shipped strings).

## Display models and mapping

Cross-feature display models live in `:core:ui` (`AccountCardUi`, `TxRowUi`); feature-specific ones (`PendingRowUi`, `RecentGroup`) live next to the feature's `UiState`. The rule from [`decisions.md`](decisions.md#architecture): a composable that takes a domain model lives in `:core:ui`; a model-agnostic one stays in `:core:designsystem`.

- **Money is formatted in the ViewModel via `MoneyFormatter`** (Koin singleton, South-Asian/lakh grouping; symbol and locale are constructor params so Settings can drive them later, defaulting to `"Rs"` / `en-IN` today). Display models carry already-formatted strings (`balanceWhole` / `balanceDecimal` split so the screen can style the fraction differently). UI never sees a raw `Money`.
- **Icons/colors travel as string keys** (`iconKey`, `colorKey`) on the display model; the screen resolves them with `categoryIcon()` / `categoryColor()`. This keeps the design system model-agnostic and the ViewModel free of Compose types.
- **`UiText` carries copy out of context-free code.** A ViewModel names a resource (`UiText.Res`, `UiText.Plural`) or wraps already-final text (`UiText.Raw`: user names, formatted money); the screen resolves it. Anything that counts uses `<plurals>`.

## Navigation and wiring

Each feature exposes one entry-point extension on `EntryProviderScope<NavKey>`, in its `navigation/` package. This is the **only** place the feature's ViewModel is instantiated and connected to navigation.

```kotlin
fun EntryProviderScope<NavKey>.homeEntry(navigator: Navigator) {
  entry<HomeRoute> {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
      state = state,
      onAllAccounts = { navigator.navigate(AccountsRoute) },
      // … map each screen callback to a navigator call or VM method
    )
  }
}
```

- **Routes are `@Serializable data object/class NavKey`s** in `:core:navigation` (`FlowFinRoutes.kt`). The five tabs are `TOP_LEVEL_ROUTES`; everything else is pushed within a tab's stack. Routes that need arguments get typed constructor fields.
- **`Navigator` mutates the back stack**; the screen just calls `navigator.navigate(SomeRoute)` / `goBack()`. Per-tab sub-stacks and single-top behavior are handled there, not in the feature.
- **Collect state with `collectAsStateWithLifecycle()`** — never a bare `collectAsState()`.
- **Register the feature in three places when adding it:** its `entryXxx()` in `FlowFinApp`'s `entryProvider { … }`, its Koin module in `FlowFinApp.onCreate()`'s `modules(...)`, and (if it's a tab) its route in `TOP_LEVEL_ROUTES` + the `TABS` list. A not-yet-built tab can register `ComingSoonScreen("X")` as its entry.

## Write flows (one-shot events and effects)

A screen that mutates (Add-Transaction) adds two things over the read-only shape:

- **Editable state lives in a `MutableStateFlow` form**, `combine`d with live option streams (accounts, categories) into the exposed `uiState`. User intents are plain VM methods that `form.update { it.copy(...) }`. Validation can use a `Form`/`FormInput` model so `canSave` is derived, not tracked by hand.
- **One-shot effects go through a `Channel`, exposed as `receiveAsFlow()`** — navigation-back, snackbar messages. These are *events*, not state (replaying them on recomposition would re-fire navigation), so they don't belong in `UiState`. The navigation entry consumes them in a `LaunchedEffect(viewModel) { viewModel.effects.collect { … } }`.

```kotlin
sealed interface AddTransactionEffect {
  data object NavigateBack : AddTransactionEffect
  data class ShowMessage(val text: String) : AddTransactionEffect
}
```

Writes call a use case and branch on `Either`: `Right` → emit a success effect; `Left` → map the typed domain error to a user message (`TransactionError.toMessage()`) and emit `ShowMessage`. The error→copy mapping is the feature's job, kept beside the effect type.

## Checklist for a new feature

1. `build.gradle.kts` — apply `flowfin.android.feature`, set namespace, add only extra deps.
2. `XUiState.kt` — sealed `UiState` with `Loading` + data-derived states; feature display models; push empties to the smallest scope.
3. `XViewModel.kt` — inject repositories + `MoneyFormatter` (+ `Clock`/`TimeZone` if time matters); `combine(...).stateIn(WhileSubscribed(5_000), Loading)`; pure `buildState`; private mapping extensions; sample `now` once.
4. `XScreen.kt` — stateless, hoisted callbacks, `when` over state, `LazyListScope` sections with stable keys, design-system components + theme tokens, `@Preview` per state.
5. `di/XModule.kt` — `viewModelOf(::XViewModel)`.
6. `navigation/XEntry.kt` — `EntryProviderScope.xEntry(navigator?)`, `koinViewModel`, `collectAsStateWithLifecycle`, wire callbacks; add a `LaunchedEffect` for effects if it's a write flow.
7. Wire into `:core:navigation` routes and `FlowFinApp` (entryProvider + Koin modules + tabs).
8. Extract copy into `:core:resources` as you build (see [`localization.md`](localization.md)); add tests that catch real regressions ([testing standards](decisions.md#quality-bar)).
