# FlowFin — implementation decisions

A running list of choices made before any Kotlin gets written. Update this when something changes.

---

## Stack

- **Jetpack Compose**, Android first. Compose Multiplatform later.
- **Koin** for dependency injection.
- **Navigation 3** (`androidx.navigation3` 1.1.2). Routes are `@Serializable` data classes (`kotlinx-serialization`).
- **SQLDelight** for the database.
- **Proto DataStore** for settings.
- **Arrow-kt** for error handling.

## Errors

- **Writes return `Either<DomainError, T>`.** An operation that can fail by a domain
  rule (duplicate name, wrong category scope, archived account, …) returns `Either`,
  and the `Left` carries an actionable, typed error.
- **Reads are plain `Flow<T>` / suspend `T?`, not `Either`.** A local SQLite read has
  no recoverable *domain* failure — a failed read is catastrophic (corrupt DB), not
  something a feature branches on, so wrapping it in `Either<DomainError, T>` would put
  a contentless `Unexpected` in every `Left` and tax every collect site for nothing.
- **No per-screen error state on reads, and no `Result`/`asResult` wrapper.** A read
  effectively always succeeds; the screen's states are `Loading` + the data-derived
  states (see Presentation). A genuine read failure means the app is broken — it is
  *not* caught and shown as a per-screen "something went wrong" (that hides the bug and
  offers no recovery). It propagates to crash reporting, and graceful degradation, if
  wanted, is a single app-wide fallback — never per-screen. (When a fallible source like
  sync/remote arrives, it gets its own error type *then*; we don't pre-empt it now.)
- Each feature has its own sealed `DomainError`. No app-wide error type.
- `try / catch` only at the real edges (database, file I/O). Mapped to `Either` on
  writes there, and never seen again above; reads aren't caught.

## Architecture

Now in Android pattern — a layered split with `:core` modules underneath and one `:feature` module per tab.

```
:app                  Android entry point
:core:designsystem    theme, components, previews (model-agnostic)
:core:ui              model-bound composables, shared UiState, MoneyFormatter, color/icon resolvers
:core:navigation      Navigation 3 route ADT (@Serializable NavKey) + Navigator + back stack
:core:database        SQLDelight schemas + queries
:core:domain          models, repository interfaces, use cases
:core:data            repository implementations
:core:common          utilities
:feature:home
:feature:accounts
:feature:transactions
:feature:recurring
:feature:debts
:feature:reports
:feature:settings
```

`:core:ui` depends on `:core:designsystem` + `:core:model`; `:core:designsystem`
stays model-agnostic. The rule: a composable that takes a domain model
(`AccountCard(account)`, `TransactionRow(txn)`) lives in `:core:ui`; a model-agnostic
M3 wrapper lives in `:core:designsystem`. Mirrors Now in Android.

## Presentation (feature → domain wiring)

- **One sealed `UiState` per screen, derived in the ViewModel.** The ViewModel is the
  single place that decides *which* state to show; the composable just `when`s over it.
  State is `combine(repo flows + UI context) { … deriveState … }.stateIn(viewModelScope,
  WhileSubscribed(5_000), Loading)` — `stateIn`'s initial value gives `Loading`; the
  sealed `UiState` gives the rest. No `Result`/`asResult`, no `.catch`. Hydration (names,
  colors, icons) is joined in the ViewModel from the small accounts/categories flows —
  compute on read, no stored joins.
- **The mockup states are data/context-derived, not error-derived.** A screen's states
  come from the *shape of the data + UI context* (active filter, range, thresholds), not
  from the error channel. Map to the empty-state taxonomy (see [`empty-states.md`](empty-states.md)):
  - **F** (full empty) — primary data empty *and* no filter → top-level `UiState` arm.
  - **N** (not-enough-yet) — data below the feature's threshold (e.g. a trend needs ≥2 points) → top-level arm, calm, no CTA.
  - **Q** (filter/search) — a filter is active *and* nothing matches → arm carrying the query to echo.
  - **S** (section empty) — screen populated but one section empty → a *field on the Content* state, rendered as an inline hint.

  There is no per-screen error state: a read failure is catastrophic (handled app-wide,
  see Errors), not one of a screen's normal states.
- **Forms** use formz (`io.github.zainulhassan815:formz`): the form `UiState` implements
  `Form`, holds `FormInput`s, gates submit on `isValid && !submissionStatus.isInProgress`,
  binds `displayError` to fields, and on submit folds the use-case `Either` → field error or success.
- **One-shot effects** (navigate, snackbar) go through a `Channel<Effect>` → `receiveAsFlow()`
  collected in the `Route` via `LaunchedEffect`. Screen-level conditions stay in `UiState`;
  the channel is only for fire-once actions that must not replay on recomposition.
- **Navigation 3.** Routes are `@Serializable` data classes/objects implementing `NavKey`
  in `:core:navigation`; a `Navigator` owns the back stack with per-tab saved stacks for the
  five tabs. IDs are typed route fields read as `key.id` (no string args), passed to the
  ViewModel via `koinViewModel { parametersOf(key.id) }`.

## Money

- Stored as `Long` minor units (paise for PKR).
- `currency` is a string tag on each account.
- Launch with PKR only. Multi-currency comes later as a per-account setting.

## Database

- Single `FlowFinDatabase` in `:core:database` (package `com.flowfin.core.database`). Schema and queries live in `.sq` files; details in [`docs/data-model.md`](data-model.md).
- IDs are UUIDv7 stored as `BLOB(16)`. Each entity has a typed inline wrapper (`AccountId`, `TransactionId`, …).
- Balances and debt-remaining are computed on read. No materialised counter columns, no caching layers (Store4/5) at the data tier. See the quality bar below.
- Driver applies `PRAGMA foreign_keys = ON` and `journal_mode = WAL` on every connection. Both are per-connection in SQLite and not persistent.
- SQLDelight 2.3 with the `sqlite-3-38` dialect. Trigger bodies must reference `new`/`old` in **lowercase** — uppercase trips the parser with "No table found with name NEW".

## Build

- AGP 9.0 + Kotlin 2.3, JDK 17 toolchain, Gradle 9.4. Kotlin Android is applied implicitly by AGP — no explicit `org.jetbrains.kotlin.android` plugin anywhere.
- Module structure mirrors Now in Android: `:core:*` for cross-cutting libraries, `:feature:*` for tabs, `:app` for the entry point.
- Convention plugins live in `build-logic/` and expose `flowfin.android.application`, `flowfin.android.library`, `flowfin.android.library.compose`, `flowfin.android.feature`, `flowfin.android.sqldelight`, `flowfin.koin`, `flowfin.jvm.library`, `flowfin.android.lint`, `flowfin.root`.

## Settings

Go in Proto DataStore, not the database. Things like reminder time, default currency, theme, sort prefs, first-time-backup state.

## Design system module

Built first, before any feature.

- Storybook-like — every component has a `@Preview` so it can be looked at in isolation.
- Covers: colour and type tokens, theme setup, fonts, the component library, and the four empty-state patterns.

## Empty states

Four treatments. Names stay the same in code as in the mockups:

- **F** — Full empty (screen takeover)
- **S** — Section empty (inline hint inside a populated screen)
- **Q** — Filter / search (notice that echoes the query)
- **N** — Not-enough-yet (calm, no CTA)

Per-screen inventory: [`docs/empty-states.md`](empty-states.md).

## Quality bar

IntelliJ-stable: predictable, no surprises, the simple thing does the right thing. Concretely:

- Optimisation is a response to a measured problem on a real device, not a hypothesis. Compute on read; cache only when something hurts.
- Every write is one atomic transaction; partial state never survives a crash. WAL + foreign keys + single-statement triggers cover this.
- Edge cases default to safe: negative balances warn (don't block), overpaid debts are allowed (forgiveness is real), the app trusts the user.
