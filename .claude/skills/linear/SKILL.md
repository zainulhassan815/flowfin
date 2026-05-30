---
name: linear
description: "Manage this project's Linear issues, projects, milestones, labels and cycles with the `linear` CLI (schpet/linear-cli). Use whenever creating, listing, viewing, updating, transitioning, or commenting on Linear issues, or doing project/milestone tracking — instead of guessing flags. The repo is pinned to workspace `flowfin-app` / team `FLO` via .linear.toml."
---

# Linear CLI (`linear`)

`schpet/linear-cli`. Run from the repo root — `.linear.toml` pins `workspace = "flowfin-app"`, `team_id = "FLO"` (the team **key**), and `issue_sort = "manual"`, so most commands need no `-w`, `--team`, or `--sort`.

Verify the user is authenticated first: `linear auth whoami`. A Linear **workspace cannot be created from the CLI** — that's web signup only.

## Gotchas (these cost real time — don't relearn them)

- **`linear issue list` shows only *your unstarted* issues by default.** For the whole board: `linear issue list --all-states -A --no-pager` (`--all-states` = every state, `-A` = all assignees). The flag is `--all-states`, NOT `--all`.
- **`-w/--workspace <slug>` is global but not honored by every subcommand.** e.g. `label list --workspace` is a *boolean* ("workspace-level labels only"), not a target. Don't fight it — rely on `.linear.toml`, or switch the active workspace with `linear auth default <slug>`.
- **Without `.linear.toml`**, `issue create` fails with "Could not determine team key" (pass `--team FLO`) and `issue list` fails needing a sort (`--sort manual|priority`). Inside this repo both are already handled.
- **`--no-interactive`** on `issue create`/`update` to avoid prompts in automation.
- **States** are given by name (`Done`, `Todo`, `In Progress`, `Backlog`, `Canceled`) or by type (`completed`, `unstarted`, `started`, `backlog`, `canceled`, `triage`).
- **`--project` and `--milestone` match by name**; `milestone create` instead needs the project **UUID** via `--project` (get it from `project create -j` or `project list`).
- **`-l/--label` is repeatable** (`-l home -l design`). Labels must already exist.
- Long markdown bodies: use `--description-file <path>`, not `-d`.

## Common recipes

```bash
# Read
linear issue list --all-states -A --no-pager
linear issue list --project "FlowFin v0 (MVP)" --milestone Home --all-states -A
linear i view FLO-13            # details;  add --web / --app to open it
linear issue id                 # the issue for the current git branch

# Create  (--team FLO only needed outside the repo)
linear issue create --no-interactive \
  -t "Accounts list screen" --description-file /tmp/body.md \
  --project "FlowFin v0 (MVP)" --milestone Accounts \
  -s Backlog -l accounts -a self

# Transition / edit  (issueId is positional)
linear issue update FLO-14 -s "In Progress" -a self
linear issue update FLO-14 -s Done
linear issue update FLO-14 --milestone Accounts -l accounts -l design -p 2

# Comment  (-b inline, or --body-file for markdown)
linear issue comment add FLO-14 -b "Shipped in <commit>."

# Structure
linear label create -n NAME -c "#82C5D4" -d "desc"        # workspace label; -t FLO for team-scoped
linear project create -n "Name" -d "desc" -t FLO -l @me -s started -j
linear milestone create --project <projectUuid> --name "Name" --description "desc"

# GitHub
linear issue pr FLO-14          # open a PR wired to the issue (see --help)
linear team autolinks           # configure FLO- autolinks on the repo
```

## This project

- Project: **"FlowFin v0 (MVP)"** (team `FLO`).
- Milestones: `Home`, `Accounts`, `Recurring`, `Debts`, `Reports`, `Settings`, `Onboarding`, `Localization`, `Release prep`.
- Labels: `design-system`, `core-data`, `core-domain`, `home`, `accounts`, `transactions`, `recurring`, `debts`, `reports`, `settings`, `onboarding`, `infra`, `docs`, `tech-debt`, `design` (+ default Bug / Feature / Improvement).

## When the CLI falls short

Every group documents itself: `linear <group> <cmd> --help` (e.g. `linear project create --help`). For anything the CLI doesn't cover, drop to the API: `linear schema` prints the full GraphQL schema and `linear api '<query>'` makes a raw request.
