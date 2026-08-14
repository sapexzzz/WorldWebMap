# Phase 04 — partial rendering/storage correctness pass

Canonical worktrees: Fabric `/home/mentality/Scripts/java-world-web-map` (`2.1fabric`, `d171c30ee664164745e6b6ed65be0e7a29018f15`) and Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` (`2.1forge`, `bc1cd58de09661bcc197df9fb207a6002586b6e1`). Both began with the accepted cumulative dirty state; no reset/recreation occurred.

## Implemented and proven

- Nether root cause: `WORLD_SURFACE` selected the roof. `SurfaceHeightResolver`, used by `TileSnapshotBuilder`, now scans from six blocks below the Nether ceiling downward, skips bedrock and air, and selects the first solid or liquid playable-cavern surface. Overworld and End retain their heightmap behavior.
- `TileStorage` now has a production path-root resolver. Services use `MinecraftServer.getWorldPath(LevelResource.ROOT)` when a server exists, with world-root storage when `saveTilesInsideWorldFolder=true` and server-root storage otherwise. The configured directory is contained by normalized-root validation.
- `/webmap fullrender` centers its existing queue loop on the actual shared spawn, using `Math.floorDiv(block, 256)`.
- Unknown vanilla web dimension input now throws instead of falling back to Overworld. Vanilla output names remain stable; custom output uses namespace-aware `namespace__path` encoding.
- Deterministic Fabric/Forge tests cover roof skipping/playable terrain/lava/open cavern, custom/default world roots, outside-world config, traversal rejection, and negative/positive spawn tile conversion.

## Validation

- Java 17 `clean check build`: **PASS** for both loaders.
- `git diff --check`: rerun after this report addition is required.
- All prior Phase 03 suites run as part of both successful full test suites.

## Remaining blockers

This phase is not complete. The following mandatory items remain: reversible custom-dimension decoding/tests; shared surface-Y usage and tests in `ApiBiomeHandler`; corrupt-existing-tile quarantine/complete-snapshot policy and tests; render-result enum/counter truthfulness tests; exact world-path behavior exercised through live service construction; and the requested End/Overworld resolver behavior tests. No runtime Minecraft smoke was performed.

PHASE 04 INCOMPLETE — DO NOT CONTINUE
