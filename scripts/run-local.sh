#!/usr/bin/env sh
set -eu

PATH="/opt/homebrew/bin:/opt/homebrew/opt/openjdk/bin:$PATH"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR"

if ! command -v java >/dev/null 2>&1 || ! command -v javac >/dev/null 2>&1; then
  echo "Java/Javac not found."
  exit 1
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "npm not found."
  exit 1
fi

(
  cd backend
  sh run-local.sh
) &
BACKEND_PID=$!

cleanup() {
  kill "$BACKEND_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

cd frontend
cp -n .env.example .env >/dev/null 2>&1 || true
npm install
npm start
