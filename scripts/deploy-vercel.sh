#!/usr/bin/env sh
set -eu

PATH="/opt/homebrew/bin:/opt/homebrew/opt/openjdk/bin:$PATH"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR"

if ! command -v vercel >/dev/null 2>&1; then
  echo "vercel CLI not found. Install with: npm i -g vercel"
  exit 1
fi

vercel --prod
