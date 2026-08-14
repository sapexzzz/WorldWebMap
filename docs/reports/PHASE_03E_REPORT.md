# Phase 03E — lifecycle ownership and chunk-state proof

## Baseline

Canonical worktrees used: Fabric `/home/mentality/Scripts/java-world-web-map` on branch `2.1fabric` at `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` at `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Both began with the cumulative Phase 02 state and uncommitted prior-phase changes (including the Phase 03/03D files); no worktree was reset or recreated. Phase 03D seams were confirmed before editing: production `PlayerMarkerService.publish(List<PlayerInfo>)` and `WebServerService` use of `HttpExecutorFactory.create()`.

Initial `git status --short` was non-clean in both canonical worktrees, as expected from cumulative uncommitted phases. Fabric had modified `.gitignore`, `build.gradle`, the entrypoint/config/commands/player/render/storage/API/web/resource files, plus untracked `docs/`, wrapper JAR, `logs/`, Phase 02 helper sources, `HttpExecutorFactory`, and `src/test/`. Forge had the corresponding modified loader files, deletion of `gradlew.orig`, and untracked license/docs/wrapper/logs/helper sources/tests. These prior changes were preserved.

## Production wiring

- Added production-used `WebMapLifecycleCoordinator` in each loader. Both `FabricWebMapMod` and `ForgeWebMapMod` delegate startup, reload, disable, shutdown, and bind/port recreation to it.
- The coordinator owns the active web-service lifecycle reference, stops it before recreating it, only starts stopped services, and makes repeated enable/disable reloads no-ops.
- It retains the current server object and calls `WebServerService.setServer` before `start`; this removes the normal-reload window in which the biome handler could observe a null server.
- `WebServerService.stop()` now always clears running state and shuts down its owned executor, including repeated-stop calls. Package-local production accessors support ownership tests.
- Extracted production-used `PendingChunkTracker`, which gates chunk pending-work admission on the real manager's `RUNNING` state. `TileRenderManager.markChunkLoaded` uses it in both loaders.

## Explicit proof tests

Both loader suites contain deterministic production-seam tests named for these contracts:

- `reloadDisablesRunningServices`, `reloadEnablesStoppedServices`, `reloadEnableIsIdempotent`, `reloadDisableIsIdempotent`
- `reloadBindKeepsServerReference`, `reloadPortKeepsServerReference`, `newWebServiceReceivesCurrentServerBeforeServingRequests`, `networkReloadStopsOldWebServiceBeforeReplacement`
- `webServerStopShutsDownOwnedExecutor`, `webServerStopIsIdempotent`, `webServerRestartReplacesExecutor`
- `disabledRenderManagerDoesNotAccumulateChunkEvents` (against the state-gated pending-work component actually used by `TileRenderManager`)

The web-service tests use the real `WebServerService` on `127.0.0.1` with port `0`; no sleeps are used. The lifecycle tests exercise the exact coordinator factories used by the loader entrypoints, rather than a separate test state model.

## Files changed

In each loader: entrypoint, `WebServerService`, `TileRenderManager`, new `lifecycle/WebMapLifecycleCoordinator`, new `render/PendingChunkTracker`, and lifecycle/web/chunk test classes. This report is the only report added, under `docs/reports/`.

## Validation

| Check | Fabric | Forge |
| --- | --- | --- |
| `clean check build` with Java 17 | PASS | PASS |
| `git diff --check` | PASS | PASS |
| Total tests | 25 | 24 |
| Phase 03D player snapshot/privacy and bounded HTTP executor tests | PASS | PASS |
| JAR version | `0.2.1` | `0.2.1` |
| JAR license | `CC0-1.0` | `CC0-1.0` |
| Loader/Minecraft constraints | unchanged (`fabricloader >=0.15.11`, Minecraft `~1.20.1`) | unchanged (Forge `47.2.0`, Minecraft `1.20.1`) |

## Remaining Phase 03 work

Only the API handler behavior/serialization tests reserved for Phase 03F remain: biome responses, player JSON, and status JSON.

PHASE 03E COMPLETE — READY FOR PHASE 03F
