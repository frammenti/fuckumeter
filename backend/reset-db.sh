#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/.env"

command="DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

PGPASSWORD="$POSTGRES_PASSWORD" \
    psql -U "$POSTGRES_USER" -d app -w -c "$command"

PGPASSWORD="test" \
    psql -U fuckumeter_test -d test -w -c "$command"