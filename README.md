# FlowFin

A quiet, editorial personal-finance app — designed to make money tracking feel
deliberate rather than urgent. This repository currently holds the **design
artifacts**: the brand, the design system, and the full set of HTML screen
mockups. Implementation lives in branches.

## Layout

```
flowfin/
├── docs/
│   └── prd.md              Product requirements
│
└── design/
    ├── system/             The design system — tokens, components, icons, brand
    │   ├── index.html        Overview
    │   ├── brand.html        Wordmark, monogram, app icon, voice & tone
    │   ├── foundations.html  Colors, typography, spacing, radius, motion
    │   ├── components.html   ~40 reusable patterns
    │   ├── icons.html        Line icon library (55+)
    │   └── styles.css        Token definitions + DS layout
    │
    └── mockups/            Live HTML screen mockups
        ├── index.html        Gallery of every screen
        ├── home/
        ├── onboarding/
        ├── accounts/
        ├── transactions/
        ├── recurring/
        ├── debts/
        ├── reports/
        └── settings/
```

## Viewing the design

The design system and mockups are static HTML. Serve them from the project root:

```bash
python3 -m http.server 8000
```

Then open:

- [http://localhost:8000/design/system/index.html](http://localhost:8000/design/system/index.html) — design system
- [http://localhost:8000/design/mockups/index.html](http://localhost:8000/design/mockups/index.html) — screen gallery

You can also open the HTML files directly in a browser — they have no build step
and no JS dependencies.

## Type & motion

- **Literata** (serif italic) for names, titles, narrative text
- **Geist Mono** (300–600) for numbers, labels, captions
- Cream accent `#E8DCC0`, deep dark base `#08080A`
- Animations stay under 200ms — functional, never decorative

See [`design/system/brand.html`](design/system/brand.html) for the voice and
tone reference, and [`design/system/foundations.html`](design/system/foundations.html)
for the token catalogue.

## Status

| | |
|---|---|
| Design system | Complete · v0.1 |
| Mockups | 22 screens shipped |
| Implementation | Not started |
