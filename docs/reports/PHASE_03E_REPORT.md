# Phase 03E — lifecycle ownership and chunk-state proof

Canonical Forge worktree: `/home/mentality/Scripts/java-world-web-map-forge-phase02b`, base SHA `bc1cd58de09661bcc197df9fb207a6002586b6e1`. It began with the cumulative Phase 02 state and existing uncommitted prior-phase changes; it was not reset or recreated. The Phase 03D production seams were confirmed: `PlayerMarkerService.publish(List<PlayerInfo>)` and `WebServerService` use of `HttpExecutorFactory.create()`.

Initial `git status --short` was non-clean from cumulative work: modified loader/build/source/resource files, deletion of `gradlew.orig`, and untracked license, docs, wrapper JAR, logs, Phase 02 helper sources, HTTP factory, and tests. Those changes were preserved.

## Production wiring and proof

- `ForgeWebMapMod` delegates startup, reload, disable, shutdown, and bind/port recreation to the production `WebMapLifecycleCoordinator`.
- It retains the current server, injects it into each new `WebServerService` before start, stops an old web service before replacement, and is idempotent for repeated same-state reloads.
- Real `WebServerService.stop()` owns and shuts down the JDK HTTP executor safely on repeated stops.
- The production-used `PendingChunkTracker` gates `TileRenderManager.markChunkLoaded` acceptance on `RUNNING`, preventing accumulation while idle or stopped.

Deterministic production-seam tests cover: `reloadDisablesRunningServices`, `reloadEnablesStoppedServices`, `reloadEnableIsIdempotent`, `reloadDisableIsIdempotent`, `reloadBindKeepsServerReference`, `reloadPortKeepsServerReference`, `newWebServiceReceivesCurrentServerBeforeServingRequests`, `networkReloadStopsOldWebServiceBeforeReplacement`, `webServerStopShutsDownOwnedExecutor`, `webServerStopIsIdempotent`, `webServerRestartReplacesExecutor`, and `disabledRenderManagerDoesNotAccumulateChunkEvents`. The web-service tests use real loopback HTTP with ephemeral port `0` and no sleeps.

## Validation

- Java 17 `./gradlew --no-daemon --console=plain clean check build`: **PASS**.
- `git diff --check`: **PASS**.
- Total tests: **24 PASS**, including the Phase 03D player snapshot/privacy and bounded HTTP executor tests.
- JAR metadata verified: version `0.2.1`, license `CC0-1.0`, Forge loader `47.2.0`, and Minecraft `1.20.1` constraints unchanged.

Remaining Phase 03 work is limited to Phase 03F API-handler behavior/serialization tests (biome, players, status).

PHASE 03E COMPLETE — READY FOR PHASE 03F
