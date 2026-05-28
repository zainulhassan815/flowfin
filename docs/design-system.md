# FlowFin — design system

Progress through the components from `design/system/components.html`. Each row is one section of that doc; "done" means the Compose component(s) ship under `:core:designsystem/component/` with co-located `@Preview` variants.

| # | Section | What's in it | Scope | Status |
|---|---|---|---|---|
| 1 | Buttons | primary, outlined, text, icon×2, FAB | medium | done |
| 2 | Status & tags | StatusTag, FrequencyTag, CountBadge | small | done |
| 3 | Form rows | FlowFinFormRow + TileIcon | medium | done |
| 4 | Toggle switch | FlowFinSwitch | small | done |
| 5 | Inputs & controls | text input · search input · segmented (pill) · underlined scope tabs | medium | done |
| 6 | Calculator pad | numeric keypad for amount entry | medium | done |
| 7 | Hero amount display | the big Geist Mono number on Home + add screens | small | done |
| 8 | Page header | screen title block (eyebrow + title + optional aux) | small | done |
| 9 | Calendar & month nav | calendar grid · month strip for Reports | medium-large | done |
| 10 | Cards | real account card · budget card with progress · pending payment card | large | done |
| 11 | List rows | transaction row · picker row with selection check | medium | done |
| 12 | Settings list | grouped settings rows (uses Switch + FormRow primitives) | small | done |
| 13 | Quick actions | the +Expense / +Income / +Transfer trio on Home | small | done |
| 14 | Insight cards | small dashboard cards (warning / positive / info) | small | done |
| 15 | Hints, banners & people | hint banner · warn banner · notification preview · person avatars · timeline | medium | done |
| 16 | Progress bars | linear progress with optional spend/budget framing | small | done |
| 17 | Navigation | bottom nav bar (5 tabs + indicator) | medium | done |
| 18 | Progress dots | onboarding pagination dots | trivial | done |
| 19 | Charts | the pie + bar charts in Reports | medium-large | done |
| 20 | Receipt thumbnail | small attachment preview | small | — |
| 21 | Bottom sheets | picker sheets (account / category / person) | medium | — |
| 22 | Empty states | the F/S/Q/N treatments — full screens | medium | — |
| 23 | Empty-state primitives | the building blocks for empty states | small | — |

## Useful groupings

- **Inputs (5)** unlocks every form (add expense, add recurring, etc.) — high leverage.
- **Page header (8) + Hero amount (7) + Quick actions (13)** make the Home screen renderable.
- **Cards (10) + List rows (11) + Navigation (17)** add the rest of Home.
- **Calendar (9) + Charts (19)** unlock Reports.
- **Bottom sheets (21)** is required by Form rows for picker UX.
- **Empty states (22, 23)** are screen-level and consume other primitives; do them last.

## Foundations

Shipped before component work began:

- `theme/Color.kt` — `FlowFinColors`, `CategoryColors`, `AvatarColors`, dark palette.
- `theme/Type.kt` — Literata + Geist Mono variable families, 9-style scale.
- `theme/Theme.kt` — `FlowFinTheme` composable + object accessor + M3 bridge.
- `preview/Foundations.kt` — storybook previews for every token group.
- `res/font/` — Literata, Literata Italic, Geist Mono variable TTFs.
