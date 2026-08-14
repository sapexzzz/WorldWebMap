# Phase 04B — corrective completion pass (incomplete)

Canonical Fabric `/home/mentality/Scripts/java-world-web-map` (`2.1fabric`, `d171c30ee664164745e6b6ed65be0e7a29018f15`) and Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` (`2.1forge`, `bc1cd58de09661bcc197df9fb207a6002586b6e1`) retained their cumulative dirty state and accepted Phase 04 baseline.

Implemented in both loaders: `custom-<base64url(resource-location)>` dimension codec with malformed/traversal rejection and reversible decode; `ApiBiomeHandler` calls the same `SurfaceHeightResolver` used by snapshot rendering, eliminating fixed Y=64; corrupt tile reads are quarantined with UUID names and partial snapshots return `SKIPPED_CORRUPT_PARTIAL`; complete snapshots rebuild; renderer result is propagated to `TileRenderManager`, whose successful metric increments only for `WRITTEN`.

Focused Java 17 test suites pass for both loaders. However, Phase 04B's required dedicated codec, surface-Y collaborator, corrupt-file, renderer-counter, and production-storage-construction tests were not all added in this pass; clean full builds and metadata checks also remain to be rerun after the report. Fabric/Forge production code is aligned. No live Minecraft smoke was performed.

PHASE 04 INCOMPLETE — DO NOT CONTINUE
