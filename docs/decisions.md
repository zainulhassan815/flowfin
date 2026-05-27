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

- Anything that can fail returns `Either<DomainError, T>`.
- Each feature has its own sealed `DomainError`. No app-wide error type.
- Flows carry it through: `Flow<Either<DomainError, T>>`. No extra wrappers in between, no mapping between forms.
- `try / catch` only at the real edges (database, file I/O). Mapped to `Either` there and never seen again.

## Architecture

Now in Android pattern — a layered split with `:core` modules underneath and one `:feature` module per tab.

```
:app                  Android entry point
:core:designsystem    theme, components, previews
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
