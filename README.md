# FlowFin

A quiet, editorial personal-finance app — designed to make money tracking feel deliberate rather than urgent.

The repository holds both the static **design artifacts** (brand, design system, full mockup gallery) and the **Android implementation** in progress against them. Implementation runs alongside design rather than after it: each design system section is ported into Compose under `:core:designsystem` as it stabilises.

## Layout

```
flowfin/
├── app/                       Android entry point (Application + MainActivity)
├── core/
│   ├── common/                shared utilities (JVM-only)
│   ├── data/                  repository implementations
│   ├── database/              SQLDelight schemas + queries + driver factory
│   ├── designsystem/          theme tokens + Compose component library
│   ├── domain/                repository interfaces, use cases (JVM-only)
│   └── model/                 plain data classes (JVM-only)
├── feature/
│   ├── home/  accounts/  transactions/  recurring/
│   └── debts/  reports/  settings/
├── build-logic/               Gradle convention plugins
├── gradle/                    version catalog + wrapper
├── design/
│   ├── system/                tokens, components, icons, brand (HTML)
│   └── mockups/               22 screen mockups across 8 surfaces (HTML)
├── docs/                      PRD, data model, decisions, design-system progress
└── experiments/sqlite/        schema + seed + queries + stress tests
```

## Stack

| | |
|---|---|
| Language | Kotlin 2.3 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Androidx Navigation 3 |
| Database | SQLDelight 2.3 (sqlite-3-38 dialect) |
| DI | Koin 4.0 |
| Errors | Arrow-kt (`Either<DomainError, T>`) |
| Settings | Proto DataStore |
| Build | AGP 9.0 · Gradle 9.4 · JDK 17 toolchain |
| Min SDK | 26 (Android 8) |
| Target SDK | 36 |

## Viewing the design

The HTML design system and mockups have no build step. Serve them from the repo root:

```bash
python3 -m http.server 8000
```

- [Design system](http://localhost:8000/design/system/index.html) — tokens, components, icons, brand
- [Mockup gallery](http://localhost:8000/design/mockups/index.html) — 22 screens

Or open the HTML files directly.

## Documentation

| | |
|---|---|
| [`docs/prd.md`](docs/prd.md) | Product requirements |
| [`docs/data-model.md`](docs/data-model.md) | Six-table schema with reasoning |
| [`docs/decisions.md`](docs/decisions.md) | Stack, architecture, quality bar |
| [`docs/design-system.md`](docs/design-system.md) | Component porting progress |
| [`docs/empty-states.md`](docs/empty-states.md) | F/S/Q/N empty-state inventory |

## Type & motion

- **Literata** (serif, italic on headings) for names, titles, narrative text
- **Geist Mono** (300–600) for numbers, labels, captions
- Cream accent `#E8DCC0`, deep dark base `#08080A`
- Animations stay under 200 ms — functional, never decorative

See [`design/system/brand.html`](design/system/brand.html) for voice and tone, [`design/system/foundations.html`](design/system/foundations.html) for the token catalogue.

## Status

| | |
|---|---|
| Mockups | 22 screens shipped |
| Design system (HTML) | complete · v0.1 |
| Data model | shipped — SQLDelight schema, 100k-row stress test, all triggers green |
| Gradle scaffolding | shipped — 14 modules build, debug APK assembles |
| Design system (Compose) | complete — all 23 sections shipped |
| Feature screens | Home — UI shell in progress (stateless, preview-driven); data/domain layer pending |
