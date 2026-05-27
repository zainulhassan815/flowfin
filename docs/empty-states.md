# Empty states inventory

A burndown of every empty state across the app, organized by screen. Each state
is tagged with a **category** (which dictates the design treatment) and a
**status** (designed / pending / N/A).

When you start designing one, switch its status. When it's shipped, link the
mockup file from its row.

---

## Categories

The treatment changes with the category — don't reuse a takeover where a hint
is enough, and don't downgrade a takeover into a hint.

| | Category | Treatment | Voice |
|---|---|---|---|
| **F** | **Full empty** | Screen takeover. Hero is muted, primary CTA replaces the populated body, FAB hidden if its action depends on missing data. | Editorial, declarative. *"Money lives somewhere. Tell us where."* |
| **S** | **Section empty** | Inline empty hint *inside* a populated screen. Short, one mono line + optional small link. Never takes over. | Quiet, factual. *"No pending payments this week."* |
| **Q** | **Filter / search empty** | Filter UI stays visible. Brief inline notice where the results would be. | Specific to the query. *"Nothing matches "rent" — try a different month."* |
| **N** | **Not-enough-yet** | Calm, informational. No CTA — the user can't act, they just need time. Often paired with a placeholder chart or faded preview. | Patient. *"We'll surface patterns once you have a month of data."* |

## Status legend

- ✓ Designed
- · Pending
- — N/A (state can't happen given the data model)

---

## 01 · Home

The first-seen surface — empty states here are the most consequential.

| State | Cat | Status | Notes |
|---|---|---|---|
| No accounts, no transactions | **F** | ✓ [home/empty.html](../design/mockups/home/empty.html) | Skipped onboarding → land on home with zero. CTA: *Add an account*. FAB hidden. |
| Has accounts, never logged a transaction | **S** | ✓ [home/no-transactions.html](../design/mockups/home/no-transactions.html) | Hero shows real balance, Pending hidden, Recent gets the inline empty hint. Re-categorized from **F** → **S** during design: the screen isn't empty when accounts/budgets are populated. |
| Accounts + transactions, but none this period | **S** | ✓ [home/no-recent-activity.html](../design/mockups/home/no-recent-activity.html) | Hero + Accounts + Pending populated. Recent section uses the inline empty hint with copy *"Nothing logged this week."* and a 6-days-ago reference. |
| Accounts exist, no recurring schedules | **S** | ✓ [home/no-recurring.html](../design/mockups/home/no-recurring.html) | Pending section absent entirely. No inline hint — the absence itself reads correctly, and the Recurring tab covers feature discovery. |
| Has transactions but < 1 month of history | **N** | ✓ [home/early-data.html](../design/mockups/home/early-data.html) | Hero meta shows clock glyph + *"Day 5 · building a picture"* in italic, replacing the green trend %. Recent trimmed to today only. |

## 02 · Onboarding

By nature these screens are empty-state-first — they *are* the empty state of
the app. No further variants needed.

| State | Cat | Status | Notes |
|---|---|---|---|
| 1-welcome / 2-real-accounts / 3-budgets / 4-reminder | — | — | The screens themselves are the empty experience. |

---

## 03 · Accounts (tab)

**Data-model note:** Budgets are allocations from real accounts (envelope-style,
not just caps). Hero meta on populated home reads *"Allocated Rs 16,000"*, and
transactions like *"Bank → Food"* literally move money from a real account
into a budget. Therefore budgets cannot exist without at least one real
account. The system should prevent that state (cascade delete, or block
deletion of the last real account when budgets exist).

| State | Cat | Status | Notes |
|---|---|---|---|
| No real accounts, no budgets | **F** | ✓ [accounts-empty.html](../design/mockups/accounts/accounts-empty.html) | Only the Real section is shown — Budget section is hidden entirely (budgets need a real account first). Single inline hint with *Add a real account* link. Hero shows muted Rs 0. |
| Real accounts exist, no budgets | **S** | ✓ [accounts-no-budgets.html](../design/mockups/accounts/accounts-no-budgets.html) | Real cards populated, Budget segment shows inline empty hint with *Set a budget* link. Hero breakdown omits the Budget chip. |
| Budgets exist, no real accounts | — | — | System prevents this state. No screen needed. |
| No archived accounts | — | — | Hide the *Archived* row entirely; never show "0 archived." |

## 04 · Account detail — real (`accounts/detail-real.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| Account just created, no transactions yet | **S** | ✓ [accounts/detail-real-empty.html](../design/mockups/accounts/detail-real-empty.html) | Hero, opening balance and quick actions all populated — only Activity is empty. Re-categorized **F** → **S** during design: the screen isn't a takeover when most of it carries the user's just-entered data. Flow strip hidden until there's flow. Inline `empty-hint` block in Activity with a `+` reference back to the FAB and quick-actions row. *Just added* chip in the hero meta marks the freshness. |
| Has transactions, none this month | **S** | · | Flow strip shows Rs 0 in / Rs 0 out for the month; activity section empty for this period. Inline hint. |

## 05 · Account detail — budget (`accounts/detail.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| Budget just created, no spending yet | **S** | ✓ [accounts/detail-budget-empty.html](../design/mockups/accounts/detail-budget-empty.html) | Hero, allocation, progress bar (at 0%, no glow), all three stats and quick actions populated — only Activity is empty. Re-categorized **F** → **S** for the same reason as the real-account just-created state. *Just allocated* chip in hero meta; *Full envelope* tag on balance label; activity meta reads *Untouched · Day 1*. Empty-hint copy: *"Nothing spent yet — that's the start, not an oversight."* Eyebrow and hint accent use `--cat-food` (the budget's own tint) instead of the global accent to reinforce envelope identity. |
| Budget had spending in past period, none this period | **S** | · | Show the period nav clearly; inline hint inside activity. |

---

## 06 · Transactions

`transactions/add.html` is a form — no empty state of its own.

Picker filter-empties for this form live under **08 · Pickers**.

---

## 07 · Recurring (tab)

| State | Cat | Status | Notes |
|---|---|---|---|
| No recurring schedules at all | **F** | ✓ [recurring/empty.html](../design/mockups/recurring/empty.html) | Both *Pending* and *Schedule* absent. Page header keeps the **+** button, history icon hidden. Specimen chips (Rent · Subs · Bills) sit above the title to telegraph what the feature captures. CTA: *Set up a recurring payment*. |
| Schedules exist, none pending right now | **S** | ✓ [recurring/no-pending.html](../design/mockups/recurring/no-pending.html) | Pending section removed entirely. A short *caught-banner* (green-tinted check glyph + *"Nothing's due right now. Next bill · Rent · 1 Jun"*) takes its place — small enough not to compete with the active list, big enough to read as good news. Stats aux line replaces *"N pending"* with a green *All caught up* pill. Active and paused-link sections unchanged from populated. |
| All schedules paused | **S** | ✓ [recurring/all-paused.html](../design/mockups/recurring/all-paused.html) | Active section empty (inline hint with *Resume one ▶* link, play-arrow glyph). Paused section surfaces inline rather than being collapsed behind the usual *View paused recurring* link — every row gets a small ▶ resume button on the right. Stats line replaces the monthly total with an *All paused* pill (pause-glyph) and reads *"0 active · 4 paused"*. Pending section hidden (nothing pending when nothing runs). New patterns locked: `.paused-row`, `.resume-btn`, and `.pause-pill` — all reusable for the has-paused-no-current edge case. |
| Has paused/archived but no current schedules | **S** | ✓ *(inherits [recurring/all-paused.html](../design/mockups/recurring/all-paused.html))* | Same render path as **All schedules paused** — Active section empty with the *Resume one* hint, Paused section populated inline. The data-model distinction (some archived vs. all paused) doesn't change what the user sees, so no separate mockup. |
| Stats / "Rs X monthly" with no schedules | **N** | — | N/A given the current design — the F empty ([recurring/empty.html](../design/mockups/recurring/empty.html)) omits the stats line entirely, so there's no monthly-total tile to replace. Re-introduce this state only if a future variant decides to show a stat tile on the empty screen. |

`recurring/add.html` is a form — no empty state.

---

## 08 · Debts (tab)

| State | Cat | Status | Notes |
|---|---|---|---|
| No debts, no receivables | **F** | ✓ [debts/empty.html](../design/mockups/debts/empty.html) | Takeover. Hero + tabs both hidden — nothing to summarise or filter. Duality glyph (Owed ↗ · Lent ↙) above the title telegraphs the two directions. CTA pair: *Record a debt* (warn-tinted) / *Record a receivable* (pos-tinted) — equal visual weight, semantic tints carry over from the populated screen's tabs. |
| Has *I owe*, no *Owe me* | **S** | · | Hide *Owe me* segment, keep *I owe* populated. No inline hint — absence is neutral. |
| Has *Owe me*, no *I owe* | **S** | · | Mirror — same treatment. |
| All settled (everything in *Settled*) | **F** | · | Special variant: hero says *"All clear."* with a faint check; settled section visible below. Different from no-debts-ever. |
| No settled history | — | — | Hide settled link entirely. |

## 09 · Debt detail (`debts/detail.html`, `debts/detail-receivable.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| Just added, only origin entry on the timeline | **S** | ✓ [debts/detail-empty.html](../design/mockups/debts/detail-empty.html) | Person hero, full amount, *Record a payment* CTA and secondary actions all stay. Progress bar at 0%; `Paid · 0%` stat shows zero in muted grey; *Full amount* chip on the balance label. Mocked on the I-owe variant — receivable mirrors with pos tint and inverted copy. Timeline shows two items: an empty placeholder card at the top (where future payments will appear, with hollow dot + dashed border + *Record one →* link) and the **origin** entry below it with the warm-amber dot. Hero meta carries a *Just added* chip. |

`add-debt.html`, `add-receivable.html`, `record-payment.html` — forms, no empty states.

---

## 10 · Reports — spending (`reports/index.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| No transactions logged ever | **F** | ✓ [reports/empty.html](../design/mockups/reports/empty.html) | Takeover. Page header strips all 3 secondary actions (insights/filter/export) — none apply. Month strip, summary, scope tabs and all charts hidden. Specimen: 7 faded category-tinted bars sketching the trend-chart form. CTA: *Log a transaction*. |
| < 1 month of data | **N** | ✓ [reports/early-data.html](../design/mockups/reports/early-data.html) | Trend chart renders 5 colored bars; remaining 26 days are dashed baselines that hold the axis width but communicate "not yet logged." The bars use category tints so the chart reads as both partial *and* informative. Axis labels stay full-month (1 · 5 today · 15 · 22 · 31) with the today index in accent. Forward-month nav arrow disabled. Summary card carries a *So far* chip on the label. An accent **n-banner** between the trend and donut reads *"Building a picture — 5 days in."* with sub *"Patterns sharpen after ~4 weeks."* Donut + breakdown render with whatever exists, donut center caption switches to *"5 days · 7 txns."* No CTA — they need time, not action. |
| Selected a past month with no activity | **Q** | ✓ [reports/no-activity.html](../design/mockups/reports/no-activity.html) | Month strip stays interactive. Summary card shows muted Rs 0 in/out. Charts/breakdown replaced by a Q-empty notice with a `Period · Jan 2026` chip eyebrow and a *Jump to May 2026* link. |
| Filter applied (category / account), no results | **Q** | ✓ [reports/filtered-empty.html](../design/mockups/reports/filtered-empty.html) | Filter chip pattern locked: rounded pill with the category marker, label, and ✕ dismiss. *Filtered by* label + chip + *Clear all* link sit above the scope tabs. Filter icon in the header turns accent-tinted while active. Q-empty notice echoes the filter terms in its eyebrow chip (`Food & Dining · May 2026`) and copies them into the title with the category color (`var(--cat-food)`). Two action links: *Clear filter* (primary) and *Pick another category*. |

## 11 · Reports — income (`reports/income.html`)

Same axes as Reports — spending. All four states inherit the spending mockups
visually; only the scope segment lands on *Income* and the accent color shifts
from neg/cat-* to `--pos` for the empty hero zeros.

| State | Cat | Status | Notes |
|---|---|---|---|
| No transactions logged ever | **F** | ✓ *(inherits [reports/empty.html](../design/mockups/reports/empty.html))* | Same takeover — there's no spending OR income axis to surface yet. |
| < 1 month of data | **N** | ✓ *(inherits [reports/early-data.html](../design/mockups/reports/early-data.html))* | Same render path with scope set to *Income*. The partial trend bars use `--pos`/income-source tints instead of expense category tints; the n-banner copy is unchanged. |
| Selected a past month with no activity | **Q** | ✓ *(inherits [reports/no-activity.html](../design/mockups/reports/no-activity.html))* | Identical render path — empty period is empty across both scopes. |
| Filter applied, no results | **Q** | ✓ *(inherits [reports/filtered-empty.html](../design/mockups/reports/filtered-empty.html))* | Same chip + notice pattern. Filter chip tint shifts to a `--pos`/income source color when filtering by income sources. |

## 12 · Reports — insights (`reports/insights.html`)

| State | Cat | Status | Notes |
|---|---|---|---|
| < 1 month of data | **N** | ✓ [reports/insights-early.html](../design/mockups/reports/insights-early.html) | Insights sheet renders over a dimmed reports backdrop. Sheet subtitle replaces the action-count chip with an accent **Day 5** chip. Body: an editorial **n-moment** (eyebrow *Patterns*, title *"Patterns surface after about a month of tracking,"* body explains the why) followed by an accent **n-progress** pill (`Day 5 / 28` with a small filled bar). Below: a single faded preview card with a dashed warn-tinted border, a *Preview* badge clipped into the top edge, and a sample *Spending watch* insight rendered with reduced opacity. No CTAs — sets expectation without promising anything actionable. |
| Exactly enough data, but no notable patterns | **N** | ✓ *(inherits [reports/insights-early.html](../design/mockups/reports/insights-early.html))* | Same sheet, copy variation only: title *"Nothing to flag this month — keep going,"* body shifts to a quiet "this is the data model behaving correctly, not a bug" line, and the n-progress pill is replaced with a small *On track* tag (no fill bar — they're already past day 28). Preview card hidden (they've seen real insights before). |
| No transactions at all | **F** | · | Inherits from Reports full-empty — probably route to that screen instead. |

---

## 13 · Settings (`settings/index.html`)

Settings is mostly populated by defaults; few real empty states.

| State | Cat | Status | Notes |
|---|---|---|---|
| No archived accounts | — | — | Hide *Archived accounts* row entirely. Already correct in the populated mockup. |
| No archived categories | — | — | Same — hide the row. |
| First-time backup (no prior backups) | **S** | · | *Backup* row sub-line says *Never backed up · last activity 27 May* instead of a date. |

## 14 · Categories (`settings/categories.html`)

Defaults always exist, so the screen is never fully empty.

| State | Cat | Status | Notes |
|---|---|---|---|
| Only defaults, no custom categories | **S** | · | Inline hint below the defaults list: *Tap + to add a custom category.* |
| Search yields no matches | **Q** | ✓ [settings/categories-search-empty.html](../design/mockups/settings/categories-search-empty.html) | Adds an active search bar at the top of the screen (accent-bordered with a glow ring and blinking caret on the query). Scope tabs (Expense / Income) stay visible — lets the user widen the search across scopes. Q-empty notice echoes the query in a serif chip and renders it as `"xyz"` in mono inside the title. Action links: *Create "xyz"* (primary, opens add-custom flow with the query pre-filled) and *Clear search*. |

## 15 · Pickers (`settings/pickers.html`)

`pickers.html` is a gallery, not a real screen — but the picker components
themselves get used inside forms and need their own empty handling.

| Picker | State | Cat | Status | Notes |
|---|---|---|---|---|
| Account picker | No accounts | **F** within sheet | · | Sheet body: *Add an account first* with a CTA that closes the sheet and routes to account-add. |
| Account picker | Search yields nothing | **Q** | ✓ *(inherits [settings/categories-search-empty.html](../design/mockups/settings/categories-search-empty.html))* | Same search-input + Q-empty pattern, rendered inside the picker sheet. The *Create "xyz"* primary link routes to add-account with the query pre-filled. |
| Category picker | No categories | — | — | Defaults always exist. |
| Category picker | Search yields nothing | **Q** | ✓ *(inherits [settings/categories-search-empty.html](../design/mockups/settings/categories-search-empty.html))* | Identical pattern inside the category-picker sheet. |
| Person picker (debts) | No contacts yet | **S** within sheet | · | First-debt scenario: just a *Add new person* row, no other entries. |
| Date picker | — | — | — | Never empty by nature. |

---

## 16 · Search (cross-app)

Not a dedicated screen yet, but mentioned in the home header. Worth deciding
the treatment before that screen is built.

| State | Cat | Status | Notes |
|---|---|---|---|
| Empty query (no input yet) | **F** within sheet | · | Recent searches OR suggestion list. Not strictly "empty." |
| Query with zero matches | **Q** | ✓ [search/empty.html](../design/mockups/search/empty.html) | Full-screen sheet (rather than a bottom sheet) since the home header search is a primary route. Close ✕ + active search input at top. Below: horizontal-scrolling **scope pills** showing zero counts (`All 0 · Transactions 0 · Debts 0 · Recurring 0 · Accounts 0`) and a *Range · Last 3 months* chip. Q-empty notice with query in a serif chip + mono callout in the title. Three actionable *try* rows: expand range, search for one term, search for the other. Each row is a tappable card with a small `Range`/`Term` mono badge on the left. |
| Query is too short (< 2 chars) | — | — | Just don't fire a search; no empty state. |

---

## Summary

| Tab / surface | Total states | Designed |
|---|---|---|
| Home | 5 | 5 |
| Onboarding | 0 | — |
| Accounts | 2 | 2 |
| Account detail (real) | 2 | 1 |
| Account detail (budget) | 2 | 1 |
| Recurring | 4 | 4 |
| Debts | 4 | 1 |
| Debt detail | 1 | 1 |
| Reports — spending | 4 | 4 |
| Reports — income | 4 | 4 |
| Reports — insights | 3 | 3 |
| Settings | 1 | 0 |
| Categories | 2 | 1 |
| Pickers | 4 | 2 |
| Search | 2 | 1 |
| **Total actionable** | **40** | **30** |

---

## Suggested design order

A path that resolves the highest-traffic surfaces first and lets us reuse
patterns:

1. ~~Home — no accounts~~ ✓
2. ~~Home — has accounts, no transactions~~ ✓
3. ~~Home — no activity this week~~ ✓
4. ~~Home — no recurring schedules~~ ✓
5. ~~Home — early data (< 1 month)~~ ✓
6. ~~Accounts — no real, no budgets~~ ✓
7. ~~Accounts — real, no budgets~~ ✓
8. ~~Recurring — no schedules~~ ✓
9. ~~Debts — no debts and no receivables~~ ✓
10. ~~Reports — no transactions ever~~ ✓
11. ~~Account / debt detail — just-added states~~ ✓
12. ~~Section empties across recurring~~ ✓
13. ~~Filter / search empties~~ ✓
14. ~~Not-enough-yet for reports & insights~~ ✓

Once the inline-hint and not-enough-yet patterns are locked in, all the
section-empty rows collapse into pattern application — no new design needed.
