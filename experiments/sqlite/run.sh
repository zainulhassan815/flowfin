#!/usr/bin/env bash
# Rebuild the DB and run the UI-driven queries.
# Usage: ./run.sh             # rebuild + run all queries
#        ./run.sh shell       # rebuild + open sqlite shell
#        ./run.sh review      # rebuild + run technical review
#        ./run.sh constraints # rebuild + run constraint smoke tests
set -euo pipefail
cd "$(dirname "$0")"

# PRAGMA foreign_keys is per-connection — must be set on every invocation.
SQL="sqlite3 -cmd 'PRAGMA foreign_keys = ON;' flowfin.db"

rm -f flowfin.db
eval "$SQL" < schema.sql
eval "$SQL" < seed.sql

case "${1:-queries}" in
  shell)       eval "$SQL" ;;
  queries)     eval "$SQL" < queries.sql ;;
  review)      eval "$SQL" < review.sql ;;
  constraints) eval "$SQL" < constraint-tests.sql ;;
  *)           echo "unknown: $1" >&2; exit 1 ;;
esac
