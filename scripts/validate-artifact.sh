#!/usr/bin/env bash
set -euo pipefail

loader=${1:?usage: validate-artifact.sh <fabric|forge> <jar>}
jar=${2:?usage: validate-artifact.sh <fabric|forge> <jar>}
test -f "$jar" || { echo "Missing artifact: $jar" >&2; exit 1; }

metadata=$(case "$loader" in
  fabric) unzip -p "$jar" fabric.mod.json ;;
  forge) unzip -p "$jar" META-INF/mods.toml ;;
  *) echo "Unknown loader: $loader" >&2; exit 2 ;;
esac)

require() { grep -Fq "$1" <<<"$metadata" || { echo "Missing/invalid metadata: $1" >&2; exit 1; }; }
case "$loader" in
  fabric)
    require '"version": "0.2.2"'; require '"license": "CC0-1.0"'
    require '"fabricloader": ">=0.15.11"'; require '"minecraft": "~1.20.1"' ;;
  forge)
    require 'version="0.2.2"'; require 'license="CC0-1.0"'
    require 'versionRange="[47.2.0,47.2.0]"'; require 'versionRange="[1.20.1,1.20.1]"' ;;
esac
echo "Validated $loader metadata: $jar"
