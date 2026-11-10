#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -n "${MSYSTEM:-}" ]] || [[ "$(uname -s)" == MINGW* ]] || [[ "$(uname -s)" == MSYS* ]]; then
  exec cmd.exe //c "$(cygpath -aw "$ROOT_DIR/mvnw.cmd")" "$@"
fi

MAVEN_VERSION="3.9.9"
MAVEN_HOME="${MAVEN_USER_HOME:-${HOME}/.m2}/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_BIN="${MAVEN_HOME}/apache-maven-${MAVEN_VERSION}/bin/mvn"
ARCHIVE="${MAVEN_HOME}/apache-maven-${MAVEN_VERSION}-bin.zip"

if [[ ! -x "$MAVEN_BIN" ]]; then
  command -v curl >/dev/null 2>&1 || { echo "curl is required to download Maven." >&2; exit 1; }
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required to extract Maven." >&2; exit 1; }
  mkdir -p "$MAVEN_HOME"
  echo "Downloading Apache Maven ${MAVEN_VERSION}..."
  curl --fail --location --silent --show-error \
    "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.zip" \
    --output "$ARCHIVE"
  unzip -q -o "$ARCHIVE" -d "$MAVEN_HOME"
fi

exec "$MAVEN_BIN" -f "$ROOT_DIR/pom.xml" "$@"
