#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

MODS_DIR="${1:-}"
MOD_ID="createtorquerails"

./gradlew build

if [ -z "$MODS_DIR" ]; then
    echo "No mods folder given, skipping copy. Usage: $0 <path-to-mods-folder>"
    exit 0
fi

if [ ! -d "$MODS_DIR" ]; then
    echo "Mods directory not found: $MODS_DIR" >&2
    exit 1
fi

rm -f "$MODS_DIR/${MOD_ID}"-*.jar

JAR=$(find build/libs -maxdepth 1 -name "${MOD_ID}-*.jar" ! -name "*-sources.jar" | head -1)
if [ -z "$JAR" ]; then
    echo "No built jar found in build/libs" >&2
    exit 1
fi

cp -v "$JAR" "$MODS_DIR/"
