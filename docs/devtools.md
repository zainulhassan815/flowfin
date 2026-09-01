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
| **Everyday — the full app** | **Start here.** Three months of an established user: Bank + Cash + JazzCash, four funded envelopes, ~100 ledger rows across every kind (income, expense, transfer, allocation, reallocation, debt), recurring in every bucket, debts both directions, custom + archived categories. The one to shoot screenshots from |
| Empty | Fresh install, no accounts — full-empty Home / Accounts |
| Accounts only | Bank + Cash + JazzCash, no activity — Account-detail opening + first-entry hint |
| Early days | Five days in — Home's "Day 5" hero, Reports' partial trend, the not-enough-yet states |
| Quiet week | Two months of history but nothing in the last ~2 weeks — Home "quiet stretch" |
| Overdue recurring | Active schedule past due — Home Pending (late) |
| Recurring schedules | Full mix — pending, overdue, upcoming across weekly/monthly/yearly, plus one paused — the Recurring tab |
| Recurring — all paused | Active section empty, paused list inline, nothing pending |
| Debts | Part-paid, untouched, off-book, overpaid, and one settled — detail timeline + settled disclosure |
| Debts — all clear | Every debt settled — the "All clear." hero |
| Negative balance | Spent more than the account holds — warning treatment |
| Over-spent budget | Envelope spend exceeds funding — progress clamp |

**Dates are calendar-anchored, not day offsets.** *Everyday* puts each month's income
and envelope funding on the 1st and repeats a spending pattern with a deterministic
per-month wobble, so Reports' month navigation always has full months behind it and no
two months read the same. Nothing is ever stamped in the future — run it on the 1st and
the current month legitimately holds only that day's rows.

Seeding also backdates accounts and categories and pulls each debt's `created_at` onto
its origin transaction, so "established user" reads consistently: Home clears its
settling window, no category claims it was created after the rows filed under it, and
a debt's audit stamp agrees with the date it says it was incurred.

## Keeping it out of release

Three independent gates: `debugImplementation` (not on the release classpath), the `src/debug` vs `src/release` `DevToolsHost` split (release compiles the chip away), and the launcher activity living only in the debug manifest.
