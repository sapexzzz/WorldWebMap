#!/usr/bin/env bash
set -euo pipefail
contains(){ if command -v rg >/dev/null 2>&1; then rg -Fq "$1" "$2"; else grep -Fq -- "$1" "$2"; fi; }
require(){ contains "$1" "$2" || { echo "Missing documentation: $1 in $2" >&2; exit 1; }; }
reject(){ ! contains "$1" "$2" || { echo "Stale documentation: $1 in $2" >&2; exit 1; }; }
require 'git switch 2.1forge' README.md
require 'build/libs/forgewebmap-0.2.1.jar' README.md
require 'maxSnapshotMillisPerTick' docs/CONFIGURATION.md
require 'fullRenderRemaining' README.md
require 'custom-<base64url(resource-location)>' README.md
reject 'cd forge' README.md
reject '../fabric/' README.md
echo 'Forge documentation checks passed'
