#!/usr/bin/env sh
set -eu

PATH="/opt/homebrew/bin:/opt/homebrew/opt/openjdk/bin:$PATH"

missing=0

check_cmd() {
  if command -v "$1" >/dev/null 2>&1; then
    echo "[ok] $1"
  else
    echo "[missing] $1"
    missing=1
  fi
}

check_any() {
  label="$1"
  shift
  for candidate in "$@"; do
    if command -v "$candidate" >/dev/null 2>&1; then
      echo "[ok] $label ($candidate)"
      return
    fi
  done
  echo "[missing] $label"
  missing=1
}

check_cmd java
check_cmd javac
check_cmd node
check_cmd npm
check_any fly fly flyctl
check_cmd vercel

if [ "$missing" -eq 1 ]; then
  echo "\nInstall missing tools before running or deploying."
  exit 1
fi

echo "\nAll required tools found."
