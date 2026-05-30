# FlowFin — strings & localization

How user-facing copy is stored and rendered. The groundwork shipped with the Home screen; new screens follow the same shape from the start.

## Where strings live

All shipped copy lives in one place: `:core:resources`, in `src/main/res/values/strings.xml`. It's a plain Android library that exists only to hold resources. Its build file sets `resourcePrefix = ""` to opt out of the library convention's auto-derived `core_resources_` prefix — shared keys are named by feature instead.

Every module reaches it transitively: `:core:resources` is wired into `AndroidFeatureConventionPlugin` (so every `:feature:*` gets it), into `:core:ui` (`api`), and into `:app`. To use a string, import `com.flowfin.core.resources.R`.

## Key naming

Feature-prefixed, lower_snake_case: `home_*`, `add_tx_*`, `nav_*`. The prefix is the surface the string belongs to, not the module. Group related keys together with a short comment header per cluster.

Use `<plurals>` for anything that counts (`N days ago`, `N days late`) — never assemble plurals by hand.

## Rendering: two paths

**In `@Composable` code, resolve directly.** Call `stringResource(R.string.…)` / `pluralStringResource(R.plurals.…, count)` at the point of use. This is the default and covers most screen copy. Composable default arguments may call `stringResource` too (e.g. a `linkLabel` that defaults to `R.string.home_link_all`).

**In context-free code (ViewModels, mappers), emit `UiText`.** A ViewModel has no `Context`, so it names the copy and lets the screen resolve it at display time. `UiText` lives in `:core:ui`:

```kotlin
sealed interface UiText {
  data class Res(@StringRes id: Int, args: List<Any> = emptyList())
  data class Plural(@PluralsRes id: Int, count: Int, args: List<Any> = emptyList())
  data class Raw(value: String)   // already-final text: user data, formatted money, etc.
}

@Composable fun UiText.asString(): String
```

Resolve it in the screen with `someUiText.asString()`. A `Res`/`Plural` arg may itself be a `UiText` — nested values are resolved before formatting, so a template can splice in another resource (Home's date label does this with a "Today" / "Yesterday" / weekday prefix). For a `Plural`, the count doubles as the sole format arg unless you pass your own.

Use `UiText.Raw` for text that's already final and shouldn't be looked up — user-entered names, money the formatter produced, separators. Mixed fields (a row name that's either a user category or an app-owned label) are `UiText` so both cases flow through one type.

## What is deliberately not in strings.xml

- **`@Preview` sample data** ("Mess Lunch", "Rs 16,000") — fixtures, not shipped copy.
- **The currency symbol** ("Rs") — it tracks the user's currency setting via `MoneyFormatter.symbol`, not the locale, so it's threaded through UI state, never "translated".
- **Weekday / month abbreviations** in date labels — still derived from English enum names (`MONDAY → Mon`). True date localization is a separate, larger effort.

## Status

- **Home** — done (VM + screens).
- **Transactions, nav / `ComingSoonScreen`, the remaining tabs** — pending; extract their copy into `:core:resources` as each is built. `AddTransactionEffect.toMessage()`'s errors become `UiText`; `InvalidAccountForKind.reason` is a domain-supplied string flagged for follow-up.
