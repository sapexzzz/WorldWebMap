# Phase 05 — performance, incremental fullrender, and render scheduling

## 1–2. Canonical worktrees and initial state

Fabric: `/home/mentality/Scripts/java-world-web-map`, branch `2.1fabric`, HEAD `d171c30ee664164745e6b6ed65be0e7a29018f15`. Forge: `/home/mentality/Scripts/java-world-web-map-forge-phase02b`, branch `2.1forge`, HEAD `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Both retained the accepted cumulative dirty worktree state; no reset or recreation occurred. `PHASE_04D_REPORT.md` was read and ends `PHASE 04 COMPLETE — READY FOR PHASE 05`.

## 3–5. Incremental fullrender, bounded state, and stop

`FullRenderPlan` is a production-used cursor held by `TileRenderManager`. It stores center/radius, current coordinate, total/generated counters, and cancellation only. `/webmap fullrender` creates it promptly and reports radius, total planned tiles, spawn tile, and incremental state. Each render tick produces at most 32 LOW-priority jobs and stops at a full queue, no renderer admission, stopped subsystem, completion, or cancellation.

For radius 1000, before: up to 4,004,001 `RenderJob` allocations/enqueue attempts in one command; after: 0 at command time, at most 32 producer allocations per render tick, and queue peak remains its existing 100,000 bound. `stopRendering` cancels the active plan, clears pending queue/pending chunk keys, and does not interrupt active worker writes.

## 6–8. Priority, tick budget, and chunk sampling

Existing HIGH > NORMAL > LOW priority remains global-capacity-bounded; fullrender uses LOW. `maxSnapshotMillisPerTick` defaults to 10ms, validates 1–1000ms, saves/reloads with the normal configuration, and bounds snapshot creation alongside `maxTilesPerTick` and admission. The forced `getChunk(..., ChunkStatus.FULL, true)` path remains only in snapshot construction when a command job requests `loadChunks`; it is now bounded by the same tick limits.

`TileSnapshotBuilder` now resolves each intersecting chunk once, then samples its 16×16 local columns. A standard 256×256 tile therefore makes approximately 256 chunk resolution calls instead of one per 65,536 column, while retaining floor-based negative-coordinate mapping, unloaded placeholders, and per-column surface resolution.

## 9–11. Metrics, frontend, and checksums

`/api/status` adds compatible `fullRenderActive` and `fullRenderRemaining` fields, alongside existing queue depth, active workers, and successful tile count. Frontend biome requests use a 150ms debounce, dimension-qualified cache key, dimension-change invalidation, abort support, and serial guards against stale responses. Synthetic rapid movement produces at most one request per debounce window.

The checksum behavior was not changed: it remains process-memory-only, so a first render after process restart may rewrite an unchanged tile. This is documented rather than risking a disk-read change to the atomic write path in this scheduling phase.

## 12–14. Tests, totals, and changed files

Required contracts are present and PASS in both loaders: fullrender bounded-state/queue/resume/spawn contracts; cancellation/queued-key/in-flight contracts; HIGH/NORMAL/LOW priority contracts; fake-clock budget and backpressure contracts; chunk traversal/unloaded/negative-coordinate contracts; and frontend debounce/cache/dimension/stale-response contracts. New test classes are `Phase05SchedulingTest`, `ChunkSamplingContractTest`, and `BiomeRequestControlTest` in both loaders.

Fresh totals: Fabric **98** tests PASS; Forge **97** tests PASS. Changed files are the render manager, new `FullRenderPlan`/`SnapshotBudget`, snapshot builder, config, commands, status API, frontend JS, Phase 05 tests, and this report in both loaders.

## 15–19. Build, metadata, diff, and regressions

Executed in each canonical worktree:

```sh
PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build
```

Fabric: PASS. Forge: PASS. All earlier Phase 02–04 regressions remain part of these passing full suites.

JAR inspection after build confirmed Fabric version `0.2.1`, license `CC0-1.0`, Fabric Loader `>=0.15.11`, Minecraft `~1.20.1`; Forge version `0.2.1`, license `CC0-1.0`, Forge `[47.2.0,47.2.0]`, Minecraft `[1.20.1,1.20.1]`.

## 20–21. Risks and parity

The remaining risk is live-server validation: real tick duration during large forced-chunk fullrenders, chunk generation behavior, and disk/worker throughput cannot be established by deterministic unit tests. Fabric and Forge implementations and tests are parity-aligned.

PHASE 05 COMPLETE — READY FOR PHASE 06
