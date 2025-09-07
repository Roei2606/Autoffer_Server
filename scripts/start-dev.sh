#!/bin/zsh
set -Eeuo pipefail

# ── הוספת נתיב uvicorn ל־PATH ────────────────────────────────
export PATH="/Library/Frameworks/Python.framework/Versions/3.12/bin:$PATH"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
mkdir -p "$ROOT/logs" "$ROOT/.pids"

# ── טעינת .env.local (אופציונלי) ─────────────────────────────
if [[ -f "$ROOT/.env.local" ]]; then
  set -a
  . "$ROOT/.env.local"
  set +a
fi

# ── נתיבים ───────────────────────────────────────────────────
DOCAI_DIR="$ROOT/DocAIMicroServer"
MEASUREMENT_DIR="$ROOT/WindowMeasurementAIService"

# ── ברירות מחדל ──────────────────────────────────────────────
: "${DOCAI_PORT:=8000}"
: "${MEASUREMENT_PORT:=8010}"
: "${PROJECT_ID:=autoffer-b606c}"
: "${LOCATION:=us}"
: "${PROCESSOR_ID:=bc83c0ca2c0dfec8}"
: "${GATEWAY_PORT:=8090}"

GOOGLE_APPLICATION_CREDENTIALS="${GOOGLE_APPLICATION_CREDENTIALS:-$ROOT/gcloud/docai-key.json}"
export PROJECT_ID LOCATION PROCESSOR_ID GOOGLE_APPLICATION_CREDENTIALS DOCAI_PORT MEASUREMENT_PORT

# ── בדיקה שמפתח ה־GCP קיים ───────────────────────────────────
if [[ ! -f "$GOOGLE_APPLICATION_CREDENTIALS" ]]; then
  echo "❌ GOOGLE_APPLICATION_CREDENTIALS לא קיים: $GOOGLE_APPLICATION_CREDENTIALS"
  exit 1
fi

# ── בחירת uvicorn גלובלי ─────────────────────────────────────
UVICORN_BIN="$(command -v uvicorn || true)"
if [[ -z "$UVICORN_BIN" ]]; then
  echo "❌ uvicorn לא נמצא במערכת. התקן עם: python3 -m pip install 'uvicorn[standard]' fastapi"
  exit 1
fi

# ── זיהוי FastAPI App עבור DocAI ─────────────────────────────
if [[ -f "$DOCAI_DIR/app/main.py" ]]; then
  DOCAI_APP="app.main:app"
elif [[ -f "$DOCAI_DIR/main.py" ]]; then
  DOCAI_APP="main:app"
elif [[ -f "$DOCAI_DIR/app.py" ]]; then
  DOCAI_APP="app:app"
else
  echo "❌ לא נמצא main של DocAI"
  exit 1
fi

# ── זיהוי FastAPI App עבור Measurement ───────────────────────
if [[ -f "$MEASUREMENT_DIR/main.py" ]]; then
  MEASUREMENT_APP="main:app"
elif [[ -f "$MEASUREMENT_DIR/app.py" ]]; then
  MEASUREMENT_APP="app:app"
else
  echo "❌ לא נמצא main של WindowMeasurementAIService"
  exit 1
fi

echo "▶︎ Starting sidecars (DocAI:$DOCAI_PORT + Measurement:$MEASUREMENT_PORT + gateway:$GATEWAY_PORT) ..."

# ── DocAI ────────────────────────────────────────────────────
(
  cd "$DOCAI_DIR"
  export PYTHONPATH="$DOCAI_DIR:${PYTHONPATH:-}"
  nohup "$UVICORN_BIN" --app-dir "$DOCAI_DIR" "$DOCAI_APP" \
    --host 0.0.0.0 --port "$DOCAI_PORT" --reload \
    > "$ROOT/logs/docai.log" 2>&1 &
  echo $! > "$ROOT/.pids/docai.pid"
)
for i in {1..60}; do
  curl -fsS "http://127.0.0.1:${DOCAI_PORT}/docs" >/dev/null 2>&1 && break
  sleep 0.5
done
if curl -fsS "http://127.0.0.1:${DOCAI_PORT}/docs" >/dev/null 2>&1; then
  echo "✅ DocAI UP (pid $(cat "$ROOT/.pids/docai.pid")) → http://localhost:${DOCAI_PORT}/docs"
else
  echo "❌ DocAI לא עלה. לוג: logs/docai.log"
  tail -n 50 "$ROOT/logs/docai.log" || true
  exit 1
fi

# ── Measurement ──────────────────────────────────────────────
(
  cd "$MEASUREMENT_DIR"
  export PYTHONPATH="$MEASUREMENT_DIR:${PYTHONPATH:-}"
  nohup "$UVICORN_BIN" --app-dir "$MEASUREMENT_DIR" "$MEASUREMENT_APP" \
    --host 0.0.0.0 --port "$MEASUREMENT_PORT" --reload \
    > "$ROOT/logs/measurement.log" 2>&1 &
  echo $! > "$ROOT/.pids/measurement.pid"
)
for i in {1..60}; do
  curl -fsS "http://127.0.0.1:${MEASUREMENT_PORT}/health" >/dev/null 2>&1 && break
  sleep 0.5
done
if curl -fsS "http://127.0.0.1:${MEASUREMENT_PORT}/health" >/dev/null 2>&1; then
  echo "✅ WindowMeasurementAIService UP (pid $(cat "$ROOT/.pids/measurement.pid")) → http://localhost:${MEASUREMENT_PORT}/docs"
else
  echo "❌ WindowMeasurementAIService לא עלה. לוג: logs/measurement.log"
  tail -n 50 "$ROOT/logs/measurement.log" || true
  exit 1
fi

# ── Gateway ──────────────────────────────────────────────────
(
  cd "$ROOT"
  nohup ./gradlew :gateway:bootRun --args="--server.port=${GATEWAY_PORT} --spring.profiles.active=dev" \
    > "$ROOT/logs/gateway.log" 2>&1 &
  echo $! > "$ROOT/.pids/gateway.pid"
)
echo "✅ Gateway starting (pid $(cat "$ROOT/.pids/gateway.pid")) → http://localhost:${GATEWAY_PORT}"
