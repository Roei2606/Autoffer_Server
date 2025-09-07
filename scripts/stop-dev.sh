#!/bin/zsh
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

stop_one () {
  local name="$1"
  local pidfile="$ROOT/.pids/$name.pid"
  if [[ -f "$pidfile" ]]; then
    local pid; pid="$(cat "$pidfile")"
    if ps -p "$pid" >/dev/null 2>&1; then
      echo "Stopping $name (pid $pid)…"
      kill "$pid" || true
      for i in {1..25}; do ps -p "$pid" >/dev/null || break; sleep 0.2; done
      ps -p "$pid" >/dev/null && kill -9 "$pid" || true
    fi
    rm -f "$pidfile"
  else
    echo "$name not running (no pidfile)"
  fi
}

# ── עצירת השירותים לפי pidfiles ──────────────────────────────
stop_one "gateway"
stop_one "measurement"
stop_one "docai"

# ── ניקוי לפי פורטים למקרה שנשארו מאזינים תלויים ─────────────
DOCAI_PORT="${DOCAI_PORT:-8000}"     # תואם ל־start-dev.sh
MEASUREMENT_PORT="${MEASUREMENT_PORT:-8010}"
GATEWAY_PORT="${GATEWAY_PORT:-8090}"

pids="$(lsof -ti tcp:${DOCAI_PORT}   || true)"; [[ -n "$pids" ]] && kill $pids 2>/dev/null || true; sleep 0.5; [[ -n "$pids" ]] && kill -9 $pids 2>/dev/null || true
pids="$(lsof -ti tcp:${MEASUREMENT_PORT} || true)"; [[ -n "$pids" ]] && kill $pids 2>/dev/null || true; sleep 0.5; [[ -n "$pids" ]] && kill -9 $pids 2>/dev/null || true
pids="$(lsof -ti tcp:${GATEWAY_PORT} || true)"; [[ -n "$pids" ]] && kill $pids 2>/dev/null || true; sleep 0.5; [[ -n "$pids" ]] && kill -9 $pids 2>/dev/null || true

echo "All sidecars stopped."
