#!/usr/bin/env sh
set -eu

PATH="/opt/homebrew/bin:/opt/homebrew/opt/openjdk/bin:$PATH"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR/backend"

if command -v fly >/dev/null 2>&1; then
  FLY_CMD="fly"
elif command -v flyctl >/dev/null 2>&1; then
  FLY_CMD="flyctl"
else
  echo "fly CLI not found. Install from https://fly.io/docs/flyctl/install/"
  exit 1
fi

"$FLY_CMD" deploy
