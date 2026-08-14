# Phase 05 — performance, incremental fullrender, and render scheduling

Phase 05 was completed in the canonical Fabric and Forge worktrees without reset or recreation: Fabric `2.1fabric` / `d171c30ee664164745e6b6ed65be0e7a29018f15`, Forge `2.1forge` / `bc1cd58de09661bcc197df9fb207a6002586b6e1`.

Both loaders now use a bounded `FullRenderPlan` cursor (4,004,001 logical tiles at radius 1000, zero eager jobs, maximum 32 produced per render tick), cancellation-aware `stopRendering`, priority-aware bounded queue behavior, 10ms configurable snapshot tick budget, chunk-oriented sampling (about 256 chunk lookups for a 256×256 tile), compatible fullrender status metrics, and 150ms frontend biome debounce/cache/stale-response protection. Checksum persistence was intentionally deferred because existing checksums are process-memory-only.

Fresh Java 17 `clean check build` passed in both loaders. Fabric ran 98 tests and Forge ran 97 tests, including all Phase 02–04 regressions and Phase 05 fullrender, stop, priority, budget, chunk, and frontend contracts. JAR metadata remains version `0.2.1`, license `CC0-1.0`, with accepted loader and Minecraft constraints. See the canonical Fabric Phase 05 report for detailed evidence and commands.

PHASE 05 COMPLETE — READY FOR PHASE 06
