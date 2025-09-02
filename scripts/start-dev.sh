#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
mkdir -p "$ROOT/logs" "$ROOT/.pids"

DOCAI_DIR="$ROOT/DocAIMicroServer"
DOCAI_PORT="${DOCAI_PORT:-5051}"

# גלה את האפליקציה
if [[ -f "$DOCAI_DIR/app/main.py" ]]; then
  DOCAI_APP="app.main:app"
elif [[ -f "$DOCAI_DIR/main.py" ]]; then
  DOCAI_APP="main:app"
elif [[ -f "$DOCAI_DIR/app.py" ]]; then
  DOCAI_APP="app:app"
else
  echo "❌ לא נמצא main של FastAPI (app/main.py | main.py | app.py)"; exit 1
fi

export GOOGLE_APPLICATION_CREDENTIALS="/Users/roeihakmon/secrets/autoffer-docai-sa.json"
export GOOGLE_CLOUD_PROJECT="autoffer-b606c"
export DOCAI_PROJECT_ID="autoffer-b606c"
export DOCAI_LOCATION="eu"
export DOCAI_PROCESSOR_ID="bc83c0ca2c0dfec8"
export DOCAI_INTERNAL_API_KEY="${DOCAI_INTERNAL_API_KEY:-some-long-secret}"

# חשוב: להריץ מתוך DocAIMicroServer (או להשתמש ב--app-dir)
(
  cd "$DOCAI_DIR"
  # אופציונלי: להבטיח ש-PYTHONPATH כולל את התיקייה
  export PYTHONPATH="$DOCAI_DIR:${PYTHONPATH:-}"
  nohup ./.venv/bin/uvicorn --app-dir "$DOCAI_DIR" "$DOCAI_APP" \
    --host 0.0.0.0 --port "$DOCAI_PORT" --reload \
    > "$ROOT/logs/docai.log" 2>&1 &
  echo $! > "$ROOT/.pids/docai.pid"
)

# המתנה שה־API יעלה
for i in {1..60}; do
  curl -fsS "http://127.0.0.1:${DOCAI_PORT}/docs" >/dev/null 2>&1 && break
  sleep 0.5
done
if curl -fsS "http://127.0.0.1:${DOCAI_PORT}/docs" >/dev/null 2>&1; then
  echo "DocAI UP (pid $(cat "$ROOT/.pids/docai.pid")) → http://localhost:${DOCAI_PORT}/docs"
else
  echo "❌ DocAI לא עלה. לוג: logs/docai.log"
  tail -n 100 "$ROOT/logs/docai.log" || true
  exit 1
fi

# ----- gateway -----
GATEWAY_PORT="${GATEWAY_PORT:-8090}"
(
  cd "$ROOT"
 nohup ./gradlew :gateway:bootRun --args="--server.port=${GATEWAY_PORT} --spring.profiles.active=dev" \
   > "$ROOT/logs/gateway.log" 2>&1 &
  echo $! > "$ROOT/.pids/gateway.pid"
)
echo "gateway starting (pid $(cat "$ROOT/.pids/gateway.pid")) → http://localhost:${GATEWAY_PORT}"
