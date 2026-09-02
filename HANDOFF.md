# FlowFin — continuing work

Android personal-finance app at `/Users/zainulhassan/Projects/flowfin`. Read `CLAUDE.md`
first, then `docs/prd.md`, `docs/launch.md`, and `design/mockups/forms/index.html`.

## State

`main` is at `86678b4`, clean, everything pushed. No open branches or PRs.
FLO-34 (Add-Budget) is Done. FLO-44 (forms rework) is In Progress — see its comment
thread, it carries the full design rationale. FLO-62…FLO-69 were filed from the last
session's findings and are all Backlog.

Last session shipped: a rebuilt `:devtools` seed; the form-system design and the port
of six of seven form screens; the Add-Budget screen; Record-payment promoted from a
bottom sheet to its own route; the FAB/header-`+` collision resolved; and two real
recurring-firing bugs fixed.

## Environment gotchas — these will waste your time otherwise

- **`java` is not on PATH.** Every gradle command needs
  `export JAVA_HOME="$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- **`adb` is not on PATH** — it lives at `~/Library/Android/sdk/platform-tools/adb`.
- **The emulator is `Medium_Phone`** (`~/Library/Android/sdk/emulator/emulator -avd Medium_Phone`).
  Same 1080×2400 @ 420dpi as the user's Pixel 7a, so tap coordinates transfer between them —
  but the emulator has **no notch**, so everything sits ~50px higher. Screenshot before
  tapping rather than reusing coordinates across devices.
- **The emulator dies when the session ends.** It runs as a background shell; a new session
  must restart it and reinstall.
- **The user's phone (Pixel 7a) pairs over Wi-Fi debugging.** USB dropped repeatedly.
  Its screen timeout is 30s, which interrupts multi-step runs — *ask* before raising it,
  and restore it afterwards. It was left at 10 minutes once when it disconnected mid-session.
- **The schema changed with no migration** (`ALLOCATION` allowed on `recurring_id`). Any
  device with an older DB must be uninstalled, not just reinstalled. "No migrations, just
  fresh" is the agreed pre-launch policy.
- **Never run two gradle invocations concurrently.** It crashed the Kotlin daemon once and
  poisoned the build cache in a way that survived cleans.
- Debug build installs two launcher icons: the app and "FlowFin Dev" (`:devtools`).
  Main activity `com.flowfin.debug/com.flowfin.MainActivity`;
  dev `com.flowfin.debug/com.flowfin.devtools.DevToolsActivity`.
- The floating **DEV chip overlays the bottom-left of every screen** — it sits on top of
  the calculator pad's `0` key. Tap `0` at x≈480, not x≈280.

## Working practices that proved out

- **Run it on the device.** Every single defect below was found by looking at a screen,
  not by building or testing. The build was green for all of them.
- **The user's screenshots find things you will not.** Three defects last session came
  from them looking: the picker-sheet gutter, the missing Weekly/Yearly cadence, and the
  "missing" Netflix transaction. Treat a screenshot as a bug report worth chasing to the
  database.
- **Check the code before theorising.** The "date glitch" was neither a date bug nor a
  glitch — one DB query settled it in seconds.
- **Look for the existing component first.** `FlowFinPickerRow` and `KeyGrid` both already
  existed and were both reinvented badly before being found.
- **Check the mockup in `design/mockups/` before building a screen.**
- **Adding a method to a repository interface breaks the test fakes** in
  `feature/accounts/AccountsTestSupport.kt` and `feature/transactions/TransactionsTestSupport.kt`.
  This has bitten four times.

## What's done, precisely

**Form system** — `design/mockups/forms/index.html` (mockup index §03b) defines the anatomy.
Ported: Add transaction, Add account, Add budget, Add recurring, Add debt, Record payment.
Shared pieces now in `:core:designsystem`: `FlowFinFormDock`, `FlowFinAmountField`,
`FlowFinKeyGrid`, plus `valueSub` on `FlowFinFormRow`.

The two rules worth remembering, because everything else follows from them:
1. **A form opens on the field it is about.** Money event → amount focused, pad up.
   Thing you name → name focused, pad down.
2. **Keypad and Save are one pinned dock**, and the pad follows focus. The dock owns no
   window insets — `FlowFinScreenScaffold` already applies them to `bottomBar`.

**Create-actions** — no screen has a bare header `+` any more. Each screen's create-action
is a full-width `FlowFinOutlinedButton` at the end of the list it belongs to. The FAB is
the only unlabelled `+` in the app and always logs a transaction, one tap, every tab.

**Budgets** — envelope stays the truth; a budget can carry a monthly *funding schedule*,
which is a recurring `ALLOCATION`. That needed a schema CHECK widened, a three-way
`RecurringSchedule.kind`, and `isExpense` excluding allocations.

## Start here

Ask the user which; the standing recommendation is **FLO-46 / FLO-47 / FLO-48** — decisions
and paperwork that gate the launch timeline. FLO-47 (`applicationId`) is irreversible after
first upload, and FLO-56's 14-day closed test is 3–4 weeks of non-compressible wall clock
that should start on a demo-quality build, not a finished one.

If they'd rather keep building: **FLO-63** (the last form), then **FLO-64** (ad-hoc
allocation — a real functional hole: an envelope that runs dry mid-month cannot be refilled),
then **FLO-21 Onboarding**.

Two decisions are waiting on the user and block their tickets: **FLO-62** (do budget funding
schedules belong in the Recurring tab?) and **FLO-68** (budget progress should read monthly
now that a monthly figure exists).

## Never verified on device

Account detail for a *real* account, Transaction detail beyond one row, the Reports insights
sheet, and every empty state except Home's. Given the hit rate above, assume defects are waiting.
