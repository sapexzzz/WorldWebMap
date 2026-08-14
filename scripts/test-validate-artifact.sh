#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd); validator="$root/scripts/validate-artifact.sh"
"$validator" fabric "$root/build/libs/fabricwebmap-0.2.1.jar"
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
negative() { local loader=$1 name=$2 content=$3 file=$4; mkdir -p "$tmp/$name/$(dirname "$file")"; printf '%s\n' "$content" > "$tmp/$name/$file"; (cd "$tmp/$name" && zip -q "$tmp/$name.jar" "$file"); if "$validator" "$loader" "$tmp/$name.jar"; then echo "negative $name unexpectedly passed" >&2; exit 1; fi; }
negative fabric fabric-missing '{}' fabric.mod.json
negative fabric fabric-version '{"version":"9.9.9","license":"CC0-1.0","depends":{"fabricloader":">=0.15.11","minecraft":"~1.20.1"}}' fabric.mod.json
negative fabric fabric-license '{"version":"0.2.1","license":"MIT","depends":{"fabricloader":">=0.15.11","minecraft":"~1.20.1"}}' fabric.mod.json
negative forge forge-missing 'x' META-INF/mods.toml
negative forge forge-version 'license="CC0-1.0"\nversion="9.9.9"\nversionRange="[47.2.0,47.2.0]"\nversionRange="[1.20.1,1.20.1]"' META-INF/mods.toml
negative forge forge-license 'license="MIT"\nversion="0.2.1"\nversionRange="[47.2.0,47.2.0]"\nversionRange="[1.20.1,1.20.1]"' META-INF/mods.toml
echo 'artifact validator positive and negative checks passed'
