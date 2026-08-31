# Working in this repo

FlowFin is an Android personal-finance app: Kotlin 2.3, Jetpack Compose + Material 3, SQLDelight, Koin, Arrow-kt. Module layout mirrors Now in Android (`:core:*` + `:feature:*` + `:app`); convention plugins live in `build-logic/`.

## Where things are

- `app/` — Application + MainActivity entry point.
- `core/database/` — SQLDelight `.sq` files in `src/main/sqldelight/com/flowfin/core/database/`, Kotlin adapters/IDs/enums in `src/main/kotlin/…/`.
- `core/designsystem/` — `theme/` for tokens (`FlowFinTheme`, `FlowFinColors`, `FlowFinTypography`), `component/` for the Compose component library, `icon/FlowFinIcons.kt` for the line-icon set, each with co-located `@Preview`s.
- `core/{common, model, domain, data}/` — JVM-only libraries except `data` which is android-library for the DB driver context. Currently empty scaffolds — models, repositories, and use cases aren't built yet (domain design is the next effort). Note: the typed IDs and schema enums presently live in `core/database`; relocating them so `core/model` can share them is part of that design.
- `feature/{home, accounts, transactions, recurring, debts, reports, settings}/` — one module per tab.
- `devtools/` — debug-only DB seeding/reset (named scenarios, wipe, reseed). On the classpath via `debugImplementation` only; never in release. See [`docs/devtools.md`](docs/devtools.md).
- `docs/` — PRD, data model, decisions, design-system progress, empty-state inventory.
- `experiments/sqlite/` — schema/seed/queries/stress tests used to validate the model before SQLDelight wiring.

## Build

```bash
./gradlew assembleDebug
./gradlew projects
./gradlew :core:designsystem:compileDebugKotlin
```

SDK path comes from `local.properties` (gitignored).

## Conventions worth knowing

- **2-space indent, no decorative banner comments.** Annotations, blank lines, and KDoc are enough — no `// ─── Section ───` rules.
- **SQLDelight triggers use lowercase `new` / `old`.** Uppercase trips the parser with "No table found with name NEW".
- **Compute on read.** Balances and debt-remaining are queries, not stored counters. No caching layers (Store-style) at the data tier; in-memory caches arrive only when a benchmark proves a need.
- **Each `.sq` file uses `AS Type` annotations** so generated row classes hold strongly-typed ids and enums; adapters are wired in `core/database/.../DatabaseFactory.kt`.
- **Design system components** mirror M3 shape — stateless, M3 under the hood, FlowFin prefix (`FlowFinButton`, `FlowFinSwitch`). One file per concept, previews colocated. The library is complete — all 23 sections of `design/system/components.html` are ported; feature screens compose from it.
- **Icons are FlowFin's line set, not Material.** `FlowFinIcons` (`core/designsystem/icon/`) holds the thin line icons ported from `design/system/icons.html`; reach for them via `categoryIcon()` (`core/ui`) for category/account keys or directly for UI glyphs, and fall back to Material only for icons the set doesn't yet cover. The set is generated — edit `design/tools/gen_flowfin_icons.py` (or the source SVGs) and re-run, don't hand-edit `FlowFinIcons.kt`.
- **Delegate complex M3 mechanics to M3.** Don't rebuild scrim/gesture/state machinery — e.g. `FlowFinModalBottomSheet` wraps M3's `ModalBottomSheet`, adding only FlowFin shape/color/handle; we supply the bespoke content (sheet headers, etc.).
- **Screens never hand-roll window insets.** The app is edge-to-edge and the shell owns no vertical insets — a pushed screen composes `FlowFinScreenScaffold` (handles status bar / nav bar / IME), a tab screen adds `statusBarsPadding()` to its top bar. Never start a screen with a bare M3 `Scaffold`. See [`docs/feature-anatomy.md`](docs/feature-anatomy.md#window-insets--edge-to-edge).
- **Driver applies `PRAGMA foreign_keys = ON` and `journal_mode = WAL` on every connection.** Both are per-connection in SQLite — not persistent.

## Quality bar

"IntelliJ-stable." Simple things do the right thing; the headline numbers don't drift; edge cases default to safe (negative balances warn, overpaid debts allowed). See [`docs/decisions.md`](docs/decisions.md#quality-bar).

## Docs to skim first

- [`docs/prd.md`](docs/prd.md) — what the product does.
- [`docs/data-model.md`](docs/data-model.md) — six tables, the kinds, the triggers, the why.
- [`docs/decisions.md`](docs/decisions.md) — stack, architecture, error handling, quality bar.
- [`docs/design-system.md`](docs/design-system.md) — the component library (all 23 sections shipped).
- [`docs/localization.md`](docs/localization.md) — where strings live (`:core:resources`) and how `UiText` carries copy out of ViewModels.
- [`docs/feature-anatomy.md`](docs/feature-anatomy.md) — how a `:feature:*` is built end to end (state, ViewModel, screen, navigation); the base shape for a new feature.
- [`docs/devtools.md`](docs/devtools.md) — the debug-only `:devtools` module: seed named DB scenarios, wipe, reseed.
- [`docs/launch.md`](docs/launch.md) — Play Store launch prep: brand research, technical blockers, store listing, sequenced plan.
