# Play Store launch preparation

Status as of 2026-08-12. Companion to [`prd.md`](prd.md) §12 (Release Plan), which
is a feature schedule; this doc is the *shipping* schedule — brand, store, account,
and the technical gaps that block an upload.

---

## 1. Brand research

### 1.1 What already exists

The brand is further along than the app. [`design/system/brand.html`](../design/system/brand.html)
is a complete identity: wordmark, monogram, app icon, clear-space and don't-do rules,
a four-axis voice guide, and three personality words. Nothing here needs inventing.

| Asset | Definition |
|---|---|
| Wordmark | `flowfin.` — Literata italic 300, cream period on the x-height |
| Monogram | `f·` tile, accent fill `#E8DCC0` on `#0A0A0B` glyph |
| App icon | `f·` on a brushed-cream tile, iOS continuous corners |
| Accent | `#E8DCC0` (sand/cream) against `#08080A` near-black |
| Tagline | *Spend with attention. Account for the rest.* |
| Voice | Quiet · Declarative · Honest · Specific — "a librarian, not a coach" |
| Personality | Editorial · Attentive · Unhurried |

The voice guide is the strongest commercial asset here. The category is saturated
with "Whoa! Food spending is getting wild this month 🍔". An expense tracker that
speaks like a printed weekly is a real differentiator, and it is already written down.

### 1.2 Name and package-ID availability — verified

Checked 2026-08-12. Method noted per row so it can be re-run.

#### Google Play — name is free

Scraped Play search for `flowfin`: **no app is titled FlowFin.** The neighbourhood is
crowded phonetically but nothing occupies the name.

| Nearest neighbour | Package |
|---|---|
| FinFlow | `com.nrvz.finflow`, `com.finflow.app`, `com.finflow.finflow` |
| Flowfy: Budget & Expenses | `com.flowfy.app` |
| Flowva | `com.chpx.flowva` |
| Flowxi | `com.flowxi.app` |
| FinanceFlo | `com.financeflo` |
| FlyFin | `com.flyfinapp` |

#### Google Play — every candidate package ID is free

`GET play.google.com/store/apps/details?id=<id>` → 404 means unclaimed. Method
validated against controls (`com.flowfy.app` → 200, a nonsense ID → 404).

| Package ID | Play |
|---|---|
| `com.flowfin` | **404 — free** |
| `com.flowfin.app` | **404 — free** |
| `app.flowfin` | **404 — free** |
| `pk.flowfin` | **404 — free** |
| `io.flowfin` | **404 — free** |
| `com.flowfin.android` | **404 — free** |

Caveat: a 404 only proves no *published* app holds it. A package ID can be reserved by
an unpublished draft in someone's console. First upload is what claims it.

#### iOS App Store — `com.flowfin.app` is already taken

`itunes.apple.com/search?term=flowfin`:

| App | Bundle ID | Seller |
|---|---|---|
| **FlowFin News** | **`com.flowfin.app`** | 金平 麻 |
| FlowFinances | `com.fauricio.controldegastos` | Julian Fauricio Valencia Marin |
| FlowFinanzas | `com.bigdataservices.flowfinanzas` | Big Data Services LLC |
| Flowfinity | `com.flowfinity.Flowfinity` | Flowfinity Wireless Inc |

iOS bundle IDs and Android application IDs are separate namespaces, so this doesn't
block Android. But [`prd.md`](prd.md) §12.1 plans an iOS submission — **`com.flowfin.app`
is dead on that side, so don't pick it for Android either** if we want one identifier
across both stores.

#### Domains — RDAP, 2026-08-12

| Domain | Status | Notes |
|---|---|---|
| `flowfin.com` | Registered 2020-01-13 | GoDaddy, serves nothing |
| `flowfin.app` | Registered 2025-03-15 | Cloudflare NS, **no A record** — parked/held |
| `flowfin.dev` | Registered 2026-08-07 | Five days old. Jellyfin plugin catalogue on GitHub Pages — unrelated hobby project |
| `getflowfin.com` | Registered 2025-12-31 | **Active FLOWFIN fintech — see below** |
| `flowfin.co.uk` | Registered 2023-11-29 | Flow Finance Ltd, UK accounting consultancy |
| `flowfin.pk` | Registered | — |
| **`flowfin.io`** | **Available** | |
| **`flowfin.co`** | **Available** | |
| **`flowfin.money`** | **Available** | |
| **`flowfin.finance`** | **Available** | |
| **`useflowfin.com`** | **Available** | |

#### The escalation: a second live FLOWFIN, in class 9

[getflowfin.com](https://getflowfin.com) trades as **FLOWFIN** — a French-language
multi-currency, multi-company financial-management SaaS for African and international
SMEs (treasury, purchasing, sales, stock, investments; XAF; "20+ enterprises equipped").
Live product, live marketing site, registered eight months ago.

This is worse than the UK consultancy. That one plausibly sits in classes 35/36
(business/financial *services*) against our class 9 (*software*). **getflowfin.com is
software.** Same name, same class, financial domain — the exact configuration that
produces examiner refusals and oppositions.

So there are now **two live commercial FLOWFIN brands in financial software**, neither
of which is us.

#### What could not be verified

Trademark registers. USPTO's search has no public GET API, Justia and Trademarkia are
behind Cloudflare (403), and the TMview API rejected the query. No FLOWFIN registration
surfaced in any indirect search — **but absence of evidence here is not clearance.**
This is the one item that must be done by hand:

- [IPO Pakistan](https://ipo.gov.pk/) — class 9, launch market, do this first
- [USPTO TESS](https://tmsearch.uspto.gov/)
- [EUIPO eSearch](https://euipo.europa.eu/eSearch/)
- [TMview](https://www.tmdn.org/tmview/) — multi-office, covers UKIPO

#### Revised recommendation

Earlier in this doc's first draft the call was "keep the name, clearance is probably
fine." The getflowfin.com find changes that to a conditional:

**Keep FlowFin only if the Pakistan class-9 search comes back clean.** Two live
software brands sharing the name in the same sector is a real risk, not a theoretical
one. The identity system is worth protecting, but it's worth less than a forced rename
after 10k installs — and a rename is far cheaper before the applicationId is burned
(§2, #9) than after.

If clearance is dirty, the fallback is a name change with the *visual* system intact:
the `f·` monogram, the cream/black palette, the Literata italic, and the voice guide
all survive a wordmark swap. Only the letterforms change.

**Actions**
- [ ] Trademark knock-out, IPO Pakistan class 9 — **blocks the applicationId decision**
- [ ] Register `flowfin.io` or `flowfin.co` now (needed for the privacy-policy URL, §4.2)
- [ ] Grab social handles before launch
- [ ] Don't use "flowfin" as a discovery term — FinFlow/Flowfy/Flowva outrank a brand
      search. Category keywords carry the Play title (§4.1)

### 1.3 Brand decisions still open

**Casing.** `brand.html` sets the wordmark lowercase (`flowfin.`); `app_name` in
`app/src/main/res/values/strings.xml` is `FlowFin`. Both are correct for their context
but the rule isn't written down. Proposal — add to brand.html §00.2:

> The wordmark is always lowercase with its period. The *name*, when set as plain
> system text — launcher label, Play title, legal copy — is `FlowFin`, no period.
> Lowercase in a launcher label reads as a typo; the wordmark is a drawn object,
> the label is a string.

**Market.** The PRD is Pakistan-first: `Rs`, JazzCash, EasyPaisa, and the whole
"works without bank sync" wedge only makes sense where no Plaid-equivalent exists.
That's a genuine, defensible position — but it needs to be a *stated* one, because it
drives default currency, launch country, screenshot content, and pricing. Decide and
write it into the PRD.

**Localisation.** `core/resources` has `values/` only. English-only is normal for
finance apps in PK and fine for v1 — but make it an explicit decision, not an oversight.
See [`localization.md`](localization.md).

### 1.4 The brand asset gap

The brand exists **only as HTML/CSS**. Nothing has been exported. Specifically:

- `app/src/main/res/` contains `strings.xml` and `themes.xml` and nothing else —
  **no launcher icon at all.** The manifest has no `android:icon`, so the app currently
  ships with the stock green Android robot.
- No 512×512 Play icon, no 1024×500 feature graphic, no screenshots.

And the existing icon design **cannot be used as-is on Android**:

| brand.html does | Android needs |
|---|---|
| iOS continuous-corner rounded square | Adaptive icon: full-bleed layers, OS applies the mask — baked corners get double-rounded |
| Drop shadow + inner highlights | Play applies its own shadow; baked shadows look muddy |
| Glyph fills most of the tile | Key content must stay inside a **66dp circle** on the 108dp canvas |
| Single flat artwork | Needs `foreground` + `background` + **`monochrome`** layer (Android 13+ themed icons) |

The `f·` in italic serif is also the hardest possible glyph for a circular mask —
the italic descender and the free-floating dot will both crowd the safe zone. Budget
real design time for an Android-native redraw of the monogram; it is not an export job.

---

## 2. Technical blockers

Found by inspection of the current tree. Ordered by how hard they block an upload.

| # | Blocker | Where | Severity |
|---|---|---|---|
| 1 | **Release build is debug-signed** — `signingConfig = signingConfigs.named("debug")` with a `TODO`. Play rejects debug-signed AABs outright. | `app/build.gradle.kts:26` | **Blocks upload** |
| 2 | **No launcher icon.** No `mipmap*` dirs, no `android:icon` in the manifest. | `app/src/main/res/`, `app/src/main/AndroidManifest.xml` | **Blocks upload** |
| 3 | **R8 is on with no rules and has never been verified.** `isMinifyEnabled = true`; `proguard-rules.pro` is two comments. A minified build that crashes is discovered at the worst moment. | `app/build.gradle.kts`, `app/proguard-rules.pro` | **Blocks upload** |
| 4 | **`allowBackup="false"` + local-only SQLite.** Users lose every manually entered transaction on device change or reinstall. For an app whose whole value is months of hand-entered data, this is a one-star engine. | `app/src/main/AndroidManifest.xml:6` | **Blocks launch** |
| 5 | **`:feature:settings` is empty** — no Kotlin sources at all. Settings is where export/import, currency, theme, about, and the privacy-policy link live. | `feature/settings/` | **Blocks launch** |
| 6 | **`:feature:reports` is a nav entry only** — one file, `ReportsEntry.kt`. Monthly reports are PRD MVP scope. | `feature/reports/` | Scope call |
| 7 | **Splash is unbranded.** `installSplashScreen()` is called but `Theme.FlowFin.Splash` inherits `android:Theme.Material.NoActionBar`, not androidx's `Theme.SplashScreen` — so no `windowSplashScreenBackground`, no animated icon, no `postSplashScreenTheme`. First frame is a default-coloured window. | `MainActivity.kt:22`, `app/src/main/res/values/themes.xml` | Polish, cheap |
| 8 | **Daily reminders unimplemented.** PRD MVP item; no notification code, no `POST_NOTIFICATIONS` in the manifest. | — | Scope call |
| 9 | **`applicationId = "com.flowfin"`** is permanent and unchangeable after first publish, and isn't the reverse of a domain we own. | `app/build.gradle.kts:12` | **Irreversible — decide now** |
| 10 | No crash reporting. The PRD targets 99.5% crash-free sessions with no way to measure it. | — | Measurement |

Non-blockers, noted: `minSdk 26` / `targetSdk 36` — already compliant with the
**API 36 requirement for new apps from 31 Aug 2026**. 21 test files repo-wide is thin
but not a gate.

### 2.1 Recommended resolutions

**#1 Signing.** Generate an upload keystore, keep it out of git, wire it from
`local.properties` or an env var, and enrol in **Play App Signing** (Google holds the
app signing key; losing the upload key becomes recoverable rather than fatal). Do this
first — it gates every other step, including the closed-test clock.

**#3 R8.** Add `./gradlew :app:bundleRelease` + an install-and-smoke-test to the
pre-flight routine now, not in week 11. Most likely rule needs: kotlinx-serialization
`@Serializable` nav route classes (Navigation 3 route types are serialized), and
anything Koin resolves reflectively.

**#4 Backup.** Ship **manual export/import** (JSON, whole-DB) rather than turning on
Auto Backup. Auto Backup + `journal_mode = WAL` can snapshot a database mid-WAL, and
we set WAL per-connection on every connection ([`CLAUDE.md`](../CLAUDE.md)). Export
also doubles as the Play data-portability story and is the #1 review request in this
category. Keep `allowBackup="false"`.

**#6 / #8 Scope.** Cut daily reminders from v1.0 — it's the largest remaining item and
drags in a runtime permission plus Play's notification policy surface. Reports should
stay: "where did it go" is the reason people open a tracker in month two. If time
forces a cut, ship reports and cut reminders, never the reverse.

**#9 applicationId.** `com.flowfin` is not the reverse of a domain we control, and the
`.co.uk` holder is a financial-services company with the same name. This is the single
decision that can never be undone after the first upload. Either register a domain and
match it (`app.flowfin` / `pk.flowfin` / `com.flowfin.app`), or accept `com.flowfin`
knowingly. Not by default.

**#10 Crash reporting.** **Play Vitals only** for v1 — it's free, automatic, needs no
SDK, and adds nothing to the Data Safety form. A third-party SDK would turn a clean
"no data collected" declaration into a disclosure exercise. Add Sentry/Crashlytics only
if Vitals proves too coarse.

---

## 3. Play Console account — the long pole

**Personal developer accounts created after 13 Nov 2023 cannot publish to production
until they run a closed test with ≥12 testers for 14 consecutive days**, then apply for
production access (Google says ≤7 days review). The 14-day clock starts only *after*
the release is approved **and** 12 testers have opted in — real devices, real Google
accounts; emulators and duplicates don't count.

This is a **minimum ~3–4 weeks of wall clock** after the first uploadable build exists,
and it is entirely non-compressible. It should start the moment the app is
demo-quality, not when it's finished.

Organization accounts (require a **D-U-N-S number**) are exempt and can publish straight
to production. Two other reasons to weigh an org account: it's the cleaner structure if
FlowFin ever monetises, and **personal accounts publish the developer's name and physical
address publicly** on the store listing.

- [ ] **Decide account type — personal vs organization.** Do this first; a D-U-N-S
      number takes days-to-weeks to obtain and changes the whole timeline.
- [ ] Register ($25, one-time), complete identity verification.
- [ ] Line up 12 real testers with real devices, in writing, before the build is ready.
      This is the step that surprises people.

---

## 4. Store listing

### 4.1 Copy

Title carries keywords, description carries the voice. That tension resolves by
splitting the jobs, not by compromising both.

**Title** (≤30 chars) — our name is not a discovery term (§1.2), so pair it with the
highest-volume category term:

| Option | Chars |
|---|---|
| `FlowFin: Expense Tracker` | 24 |
| `FlowFin: Budget & Expenses` | 26 |
| `FlowFin: Expense & Budget` | 25 |

Recommend the first — "expense tracker" is the head term and matching it exactly matters
more than fitting a second keyword in.

**Short description** (≤80 chars) — highest-weight field after the title:

> `Expense tracker & budget planner. Fully offline — no bank login, no ads.` (72)

**Full description** (≤4000) — this is where the voice lives. Draft opening:

> Most money apps tell you where your money went. FlowFin tells you what's left.
>
> Everything in FlowFin is an account with a balance. Your bank is an account. Your
> cash is an account. So is your food budget. Money comes in, moves between, and goes
> out — the same way it does in life. Fund your budgets first, spend only from funded
> accounts, and know exactly what remains.
>
> No bank connection. No sync. No subscription. Your data never leaves your phone.

Then plain feature lines — no emoji, no exclamations, per the voice guide. Keywords
(expense tracker, budget planner, spending, debt tracker, offline, recurring bills)
belong here, worked into sentences, not stuffed.

### 4.2 Required assets and forms

| Item | Spec | Status |
|---|---|---|
| App icon | 512×512, 32-bit PNG with alpha, ≤1 MB, **no baked corners or shadow** | Missing |
| Feature graphic | 1024×500, JPEG or 24-bit PNG, **no alpha**; keep content centred — Play crops edges and overlays the icon | Missing |
| Phone screenshots | ≥2 (use 6–8), 1080×1920, sides 320–3840px, max 2:1 | Missing |
| Privacy policy | Public URL, required for every app | Missing — needs the domain (§1.2) |
| Data safety form | 14 categories; declare **no data collected / no data shared**. Must match the binary and the privacy policy or review flags it | Not started |
| Content rating | IARC questionnaire → Everyone | Not started |
| Target audience | 18+ (or 13+). Not children — avoids Families policy entirely | Not started |
| Ads declaration | No ads | Not started |
| Financial features declaration | Applies to lending / crypto / investment apps. Manual tracking shouldn't trigger it — but the **Debts** feature will draw a reviewer's eye, so state plainly in the listing that FlowFin records debts you already have and never lends, brokers, or connects to a bank | Watch |

Screenshots are the highest-leverage asset in the listing and the one most often
rushed. The editorial identity — generous margins, serif, restrained colour — will look
genuinely distinct next to the neon gradient screenshots that fill this category. Shoot
them from `:devtools` seeded scenarios so the numbers are realistic rather than
`$1,234.56` placeholders.

---

## 5. Sequenced plan

Ordered so the slow, blocking, and irreversible things start first. Tracked in
Linear under **FlowFin v0 (MVP) → Release prep**.

### Phase 0 — Unblock (start now, ~1 week, parallel with feature work)

| | Issue |
|---|---|
| Decide **personal vs organization** account; if org, start the D-U-N-S application today | FLO-46 |
| Decide the **`applicationId`** — irreversible after first upload | FLO-47 |
| Generate upload keystore, wire release signing, enrol in Play App Signing | FLO-48 |
| Trademark knock-out search (IPO Pakistan, class 9) + domain and handles | FLO-50 |
| Register the Play Console account, complete identity verification | FLO-51 |

### Phase 1 — Make it uploadable (runs alongside remaining feature work)

| | Issue |
|---|---|
| **Android icon set**: adaptive `foreground`/`background`/`monochrome` + 512 Play icon (redraw, not export) | FLO-49 |
| Verify `bundleRelease` — install the minified AAB, walk every screen, fix R8 rules | FLO-52 |
| **Export/import** so users don't lose data on reinstall | FLO-53 |
| Write and host the privacy policy | FLO-54 |
| Fix the splash theme to androidx `Theme.SplashScreen` with the brand background | FLO-57 |
| Build `:feature:settings` (currency, theme, about, policy link) | FLO-20 |
| Finish `:feature:debts`, then `:feature:reports` | FLO-18, FLO-19 |

### Phase 2 — Listing (needs Phase 1's icon + a screenshot-able app)

| | Issue |
|---|---|
| Final title / short / full description | FLO-58 |
| Feature graphic and 6–8 screenshots from seeded devtools scenarios | FLO-59 |
| Data safety, content rating, target audience, ads declarations | FLO-60 |

### Phase 3 — Closed test (**the 3–4 week wall clock — start the day a demo-quality build exists**)

| | Issue |
|---|---|
| Recruit and confirm 12 testers with real devices | FLO-55 |
| Upload, confirm 12 opt-ins, 14 consecutive days, apply for production access | FLO-56 |

### Phase 4 — Production

| | Issue |
|---|---|
| Staged rollout Pakistan-first, Play Vitals watch, respond to every review | FLO-61 |

**Critical path:** account type → keystore → uploadable build → 14-day closed test →
production review. Phases 1 and 2 fit inside Phase 3's wall clock if the closed test
starts on a demo-quality build rather than a finished one. Start it early.

---

## Sources

- [App testing requirements for new personal developer accounts — Play Console Help](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)
- [Provide information for Google Play's Data safety section — Play Console Help](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en)
- [Google Play Closed Testing in 2026: 12 Testers to Production Access](https://medium.com/@kefayatkhadem/google-play-closed-testing-in-2026-the-full-path-from-12-testers-to-production-access-1f48b7833671)
- [Google Play Closed Testing Requirements for New Personal Dev Accounts (2026)](https://aerious.uk/blog/google-play-closed-testing-requirements-for-new-personal-developer-accounts-2026)
- [Google Play & App Store Screenshot Sizes 2026 — Specs & Listing Guide](https://www.choicely.com/tutorials/google-play-app-store-guidelines-screenshots-listings)
- [Google Play Feature Graphic Size 2026](https://screenkit.tools/specs/google-play-feature-graphic-size)
- [Google Play Data Safety Form: 2026 Requirements Guide](https://respectlytics.com/blog/google-play-data-safety-guide/)
- [Google Play ASO Keywords: The 2026 Practitioner's Guide](https://appfollow.io/blog/google-play-aso-keywords)
- [App Store Optimization Title: 2026 ASO Title Playbook](https://appfollow.io/blog/app-store-optimization-title)
- [FlowFin — Accounting that Flows (flowfin.co.uk)](https://flowfin.co.uk/)
- [Flow Finance — Dealroom](https://app.dealroom.co/companies/flow_finance)
- [FinFlow — Google Play](https://play.google.com/store/apps/details?id=com.nrvz.finflow)
- [Flowfy: Budget & Expenses — Google Play](https://play.google.com/store/apps/details?id=com.flowfy.app)
