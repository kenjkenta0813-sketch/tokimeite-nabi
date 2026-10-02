#!/usr/bin/env sh
set -eu

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle is not installed or is not on PATH. Install Android Studio or Gradle 8.x and rerun this command." >&2
  exit 1
fi
