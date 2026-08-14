# Phase 04 — partial rendering/storage correctness pass

Canonical Forge worktree: `/home/mentality/Scripts/java-world-web-map-forge-phase02b` (`2.1forge`, `bc1cd58de09661bcc197df9fb207a6002586b6e1`). Cumulative state was preserved.

Implemented: production Nether `SurfaceHeightResolver` used by `TileSnapshotBuilder`; server/world-root-aware tile storage resolver honoring `saveTilesInsideWorldFolder`; actual-spawn fullrender center with floor division; and strict unknown-dimension rejection. Deterministic tests cover Nether roof/playable/lava/open-cavern selection, root containment/default/custom paths, and spawn coordinates. Java 17 `clean check build` passed.

Mandatory work remains for biome surface-Y sharing, reversible custom-dimension decoding, corrupt-tile policy, render counter results, and live-service path proof. No runtime smoke was performed.

PHASE 04 INCOMPLETE — DO NOT CONTINUE
