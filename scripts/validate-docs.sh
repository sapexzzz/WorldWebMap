#!/usr/bin/env bash
set -euo pipefail
require(){ rg -Fq "$1" "$2" || { echo "Missing documentation: $1 in $2" >&2; exit 1; }; }
reject(){ ! rg -Fq "$1" "$2" || { echo "Stale documentation: $1 in $2" >&2; exit 1; }; }
require 'git switch 2.1fabric' README.md
require 'build/libs/fabricwebmap-0.2.1.jar' README.md
require 'maxSnapshotMillisPerTick' docs/CONFIGURATION.md
require 'fullRenderRemaining' README.md
require 'custom-<base64url(resource-location)>' README.md
reject 'cd fabric' README.md
reject '../forge/' README.md
echo 'Fabric documentation checks passed'
