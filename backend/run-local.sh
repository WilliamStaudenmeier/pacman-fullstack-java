#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")"
mkdir -p out data
javac -d out src/Main.java
PORT=${PORT:-8080} java -cp out Main
