# Phase 05B — final performance/scheduler verification

## 1–3. Canonical baseline

Fabric `/home/mentality/Scripts/java-world-web-map`: branch `2.1fabric`, HEAD `d171c30ee664164745e6b6ed65be0e7a29018f15`. Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b`: branch `2.1forge`, HEAD `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Both began with their accepted cumulative dirty statuses; no worktree was reset or recreated.

Confirmed production baseline in both: `FullRenderPlan` is used by `TileRenderManager`; the LOW-priority producer is bounded at 32 jobs/tick and honors queue/admission state; `stopRendering` cancels plans and clears queued/pending work; snapshot time budget and chunk-oriented traversal are present; status exposes fullrender fields; and frontend has debounce/cache/dimension/serial handling.

## 4. Exact required tests — all PASS in both loaders

- Fullrender: `fullRenderRadius1000DoesNotMaterializeMillionsOfJobs`, `fullRenderProducerStopsWhenQueueIsFull`, `fullRenderProducerResumesWhenCapacityReturns`, `fullRenderPlanUsesBoundedState`, `fullRenderUsesActualSpawn`.
- Stop: `stopRenderCancelsActiveFullRenderPlan`, `stopRenderPreventsFurtherPlanProduction`, `stopRenderClearsQueuedJobs`, `stopRenderLeavesNoStaleTileKeys`, `stopRenderDoesNotCorruptInFlightTileWrite`.
- Priority: `highPriorityRenderPreemptsPendingFullRenderJobs`, `normalPriorityPreemptsLowPriorityFullRender`, `globalQueueCapacityStillAppliesAcrossPriorities`.
- Tick budget: `snapshotBudgetStopsWorkWithinTick`, `snapshotBudgetResetsNextTick`, `maxTilesPerTickStillApplies`, `backpressureStopsSnapshotCreation`.
- Chunk sampling: `standardTileResolvesEachChunkAtMostOnce`, `chunkOrientedSamplingProducesSameCoordinates`, `unloadedChunkPixelsRemainMarkedUnloaded`, `surfaceResolverStillUsedForEveryColumn`, `negativeCoordinatesMapToCorrectChunks`.
- Frontend: `rapidMouseMovementProducesBoundedBiomeRequests`, `biomeCacheKeyIncludesDimension`, `dimensionSwitchInvalidatesBiomeState`, `staleBiomeResponseCannotOverwriteNewerRequest`.
- Snapshot configuration: `snapshotBudgetDefaultMinimumMaximumAndPersistence`, `malformedSnapshotBudgetResetsToDefault`.

## 5–6. Boundedness and admission proof

At radius 1000, logical total is `4,004,001`. Command-time eager `RenderJob` allocations are zero: the command only creates the O(1) `FullRenderPlan` cursor/counters. The producer can admit at most 32 jobs per render tick, does not advance its cursor on a rejected offer, and `BoundedRenderQueue` retains global capacity 100,000 across priorities. Renderer admission is `renderThreads * 2` permits (default worker count 1, admission bound 2); snapshot creation stops before polling/building when no permit is available.

## 7. Stoprender proof

`stopRendering` cancels and detaches the plan, clears pending queue jobs and pending chunk keys, and leaves active worker jobs protected until their normal completion callback releases the key. It never interrupts the renderer or its atomic file replacement.

## 8. Snapshot budget configuration

`maxSnapshotMillisPerTick` defaults to 10ms; valid range is 1–1000ms; malformed input resets defaults under existing config policy; save/reload preserves accepted values. Fabric and Forge have identical semantics and regression coverage.

## 9. Chunk-resolution proof

A 256×256 tile spans 16×16 = 256 chunks. Production traversal resolves each intersecting chunk once, then samples its local columns; every loaded column still invokes `SurfaceHeightResolver`; unloaded pixels retain their placeholder mask; floor division preserves negative coordinates. These are boundedness proxies, not claims of live TPS improvement.

## 10. Frontend proof

Focused static tests inspect the production `app.js`: 150ms `setTimeout` debounce/`clearTimeout`, cache key `dimension:x:z`, dimension switch resetting/re-evaluating biome state, and `serial === _biomeRequestSerial` stale-response guard. `AbortController` remains in the request path.

## 11. Checksum limitation

Checksums remain process-memory-only. The first unchanged render after process restart may rewrite its PNG. This is a known non-blocking optimization limitation, not a correctness or release blocker.

## 12–17. Validation and metadata

Full Java 17 commands run:

```sh
PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build
```

Fabric: PASS, **100** tests. Forge: PASS, **99** tests. Earlier Phase 02–04 regressions are included in the successful full suites.

Metadata commands:

```sh
unzip -p build/libs/fabricwebmap-0.2.1.jar fabric.mod.json
unzip -p /home/mentality/Scripts/java-world-web-map-forge-phase02b/build/libs/forgewebmap-0.2.1.jar META-INF/mods.toml
```

Fabric: version `0.2.1`, license `CC0-1.0`, Fabric Loader `>=0.15.11`, Minecraft `~1.20.1`. Forge: version `0.2.1`, license `CC0-1.0`, Forge `[47.2.0,47.2.0]`, Minecraft `[1.20.1,1.20.1]`.

## 18–21. Files, final checks, risk, parity

Phase 05B changed only the cursor’s rejected-offer behavior, null-safe `RenderJob` equality/hash support for its regression seam, snapshot-budget config tests, and this report. Fabric/Forge parity is retained. Remaining risk is limited to live-server tick performance, forced chunk generation behavior, and real-world disk/worker throughput.

FINAL POST-REPORT `git diff --check`: Fabric PASS; Forge PASS.

PHASE 05 COMPLETE — READY FOR PHASE 06
