# FlowFin — dev tools

The `:devtools` module puts the database into known states so screens and edge cases can be exercised without hand-entering data. It's **debug-only** — present on the debug classpath via `debugImplementation(projects.devtools)` and absent from release entirely.

## Two entry points (both debug-only)

- **The "DEV" chip** — a hideable overlay the debug app wraps itself in (`DevToolsHost`, `app/src/debug`). Its `src/release` twin is a pure passthrough, so neither the chip nor any reference to the module reaches release. Tapping it opens the dev-tools screen.
- **A "FlowFin Dev" launcher icon** — a second activity (`DevToolsActivity`) merged into the debug APK's manifest only. Same screen, reachable straight from the launcher.

## How it wires

It runs in the app process, so the global Koin the `Application` started is already up. `DevToolsActivity` loads `devModule` once on top of it and resolves `DevScenarios` from the **real** repositories and use cases. Seeding therefore goes through production code paths — balances, "pending" thresholds, and debt-remaining behave exactly as they do live.

## What it does

- **`DevScenarios.run(scenario)`** — wipes the DB, then seeds the named state through the domain use cases. Rows are dated relative to "now", so time-sensitive states (pending / this-week / this-month) stay valid whenever you run them.
- **`wipe()`** — empties everything (`DevReset` deletes all tables in one transaction, children before parents, so foreign keys never trip).
- **`reseedCategories()`** — restores the default category set.

## Scenarios

Each maps to the screens/states it lets you check (see [`empty-states.md`](empty-states.md)):

| Scenario | Exercises |
|---|---|
| Empty | Fresh install, no accounts — full-empty Home / Accounts |
| Accounts only | Bank + Cash, no activity — Account-detail opening + first-entry hint |
| Loaded month | Accounts, budgets, salary + spend — Home full, flow strip |
| Quiet week | History but nothing in the last ~2 weeks — Home "quiet stretch" |
| Overdue recurring | Active schedule past due — Home Pending (late) |
| Debts | One I-owe + one owed-to-me |
| Negative balance | Spent more than the account holds — warning treatment |
| Over-spent budget | Envelope spend exceeds funding — progress clamp |

## Keeping it out of release

Three independent gates: `debugImplementation` (not on the release classpath), the `src/debug` vs `src/release` `DevToolsHost` split (release compiles the chip away), and the launcher activity living only in the debug manifest.
