#!/usr/bin/env bash
#
# Secret Santa runner.
#
#   ./santa.sh dry-run   <profile> [--year YYYY]          match only, print pairs, send nothing
#   ./santa.sh run       <profile> [--year YYYY] [--yes] [--force]
#                                                         match + send real emails, log to
#                                                         src/main/resources/participants/<year>_<name>.log
#   ./santa.sh smtp-test [<profile>] [--year YYYY]        send ONE test email to internal.default-email
#
# `run` matches first, shows the pairs and asks for "send" before any email goes out (--yes skips that).
# Anything after `--` is passed to the app as-is, e.g. -- --internal.use-test-email=true
# <profile> is a profile defined in src/main/resources/application-secret.yml (e.g. fwb, kids).
# The "secret" profile is always active. Year defaults to the current year.
#
# Env: SECRET_SANTA_JAVA_HOME - JDK to use (defaults to JDK 21; Kotlin 1.9 does not build on JDK 25).

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
RESOURCES="$ROOT/src/main/resources"
SECRET_YML="$RESOURCES/application-secret.yml"
LOG_DIR="$RESOURCES/participants"

usage() { sed -n '3,17p' "$0" | sed 's/^# \{0,1\}//'; exit "${1:-1}"; }
die() { echo "error: $*" >&2; exit 1; }

# Log file name per profile, to stay consistent with the existing logs (2025_dorośli.log, ...).
log_name_for() {
  case "$1" in
    fwb) echo "dorośli" ;;
    *)   echo "$1" ;;
  esac
}

# ---- args ------------------------------------------------------------------
[ $# -ge 1 ] || usage
MODE="$1"; shift
case "$MODE" in dry-run|run|smtp-test) ;; -h|--help|help) usage 0 ;; *) usage ;; esac

PROFILE=""; YEAR="$(date +%Y)"; YES=false; FORCE=false; EXTRA_ARGS=""
while [ $# -gt 0 ]; do
  case "$1" in
    --) shift; EXTRA_ARGS="$*"; break ;;
    --year)  [ $# -ge 2 ] || die "--year needs a value"; YEAR="$2"; shift 2 ;;
    --yes|-y) YES=true; shift ;;
    --force) FORCE=true; shift ;;
    -h|--help) usage 0 ;;
    -*) die "unknown option $1" ;;
    *)  [ -z "$PROFILE" ] || die "unexpected argument $1"; PROFILE="$1"; shift ;;
  esac
done

[[ "$YEAR" =~ ^[0-9]{4}$ ]] || die "year must be 4 digits, got '$YEAR'"
[ -f "$SECRET_YML" ] || die "missing $SECRET_YML (see README)"

if [ "$MODE" != "smtp-test" ] && [ -z "$PROFILE" ]; then
  die "profile is required for $MODE"
fi
if [ -n "$PROFILE" ] && ! grep -Eq "on-profile:[[:space:]]*\"?$PROFILE\"?[[:space:]]*$" "$SECRET_YML"; then
  die "profile '$PROFILE' not found in application-secret.yml. Available: $(grep -E 'on-profile:' "$SECRET_YML" | awk '{print $2}' | tr '\n' ' ')"
fi

# ---- java ------------------------------------------------------------------
if [ -n "${SECRET_SANTA_JAVA_HOME:-}" ]; then
  export JAVA_HOME="$SECRET_SANTA_JAVA_HOME"
elif [ -x /usr/libexec/java_home ] && JH="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
  export JAVA_HOME="$JH"
fi

# ---- app args --------------------------------------------------------------
PROFILES="secret${PROFILE:+,$PROFILE}"
APP_ARGS="--spring.profiles.active=$PROFILES --internal.year=$YEAR"
case "$MODE" in
  dry-run)   APP_ARGS="$APP_ARGS --internal.do-send=false --internal.use-test-email=false" ;;
  run)       APP_ARGS="$APP_ARGS --internal.do-send=true --internal.use-test-email=false --internal.confirm-before-send=$( $YES && echo false || echo true )" ;;
  smtp-test) APP_ARGS="$APP_ARGS --internal.smtp-test=true" ;;
esac
for ARG in $EXTRA_ARGS; do  # extra args override the defaults above (Spring would merge duplicates)
  KEY="${ARG%%=*}"
  APP_ARGS="$(printf '%s' " $APP_ARGS " | sed "s| $KEY=[^ ]*| |") $ARG"
done

run_app() { "$ROOT/gradlew" -p "$ROOT" -q --console=plain bootRun --args="$APP_ARGS"; }

print_pairs() {  # extracts "Matched participants: a -> b, c -> d" into one pair per line
  grep -m1 -o 'Matched participants: .*' "$1" | sed 's/^Matched participants: //' | tr ',' '\n' | sed 's/^ */  /'
}

echo "mode=$MODE profile=${PROFILE:-<none>} year=$YEAR java=${JAVA_HOME:-<system>}"

case "$MODE" in
  dry-run)
    TMP_LOG="$(mktemp -t secret-santa-dry-run)"
    trap 'rm -f "$TMP_LOG"' EXIT
    run_app 2>&1 | tee "$TMP_LOG"
    echo
    echo "=== Dry run pairs ($PROFILE, $YEAR) - nothing was sent ==="
    print_pairs "$TMP_LOG"
    ;;

  smtp-test)
    run_app
    echo
    echo "=== SMTP test OK - check the inbox of internal.default-email ==="
    ;;

  run)
    LOG_FILE="$LOG_DIR/${YEAR}_$(log_name_for "$PROFILE").log"
    if [ -e "$LOG_FILE" ] && ! $FORCE; then
      die "$LOG_FILE already exists - emails for '$PROFILE' $YEAR were probably already sent. Use --force to run anyway."
    fi
    echo "Log: $LOG_FILE"
    [ -e "$LOG_FILE" ] && mv "$LOG_FILE" "$LOG_FILE.$(date +%Y%m%d%H%M%S).bak"
    set +e
    run_app 2>&1 | tee "$LOG_FILE"
    STATUS=${PIPESTATUS[0]}
    set -e
    echo
    if grep -q "SENDING ABORTED" "$LOG_FILE"; then
      rm -f "$LOG_FILE"
      echo "=== Aborted - no emails were sent, log removed. ==="
      exit 0
    fi
    if [ "$STATUS" -ne 0 ]; then
      echo "!!! Run FAILED (exit $STATUS). Some emails may have been sent - check $LOG_FILE before re-running (--force)." >&2
      exit "$STATUS"
    fi
    echo "=== Done. Log written to $LOG_FILE ==="
    ;;
esac
