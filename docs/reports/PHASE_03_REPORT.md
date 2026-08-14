# Phase 03 — lifecycle, privacy, HTTP, and Minecraft-thread safety

Canonical worktrees used: Fabric `/home/mentality/Scripts/java-world-web-map` at base `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` at base `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Both had `docs/reports/CUMULATIVE_STATE_AFTER_PHASE_02.md` and the accepted Phase 01/02/02B files before editing.

## Implemented changes

- Reload now keeps a current `MinecraftServer`; network recreation calls `setServer` after starting the replacement service. Enabled-to-disabled stops render and HTTP services; disabled-to-enabled starts them again against the retained server. Repeated disable/enable is guarded by actual running state.
- `TileRenderManager.markChunkLoaded` now accepts only `RUNNING` state in both loaders, preventing Fabric IDLE/stopped pending accumulation.
- Player markers are published as immutable server-tick snapshots. HTTP `/api/players` no longer iterates live player objects. Disabled markers immediately clear the snapshot and return `[]`.
- Biome requests schedule the world read onto the Minecraft server thread, wait at most 250ms, and return a transient 503 on timeout/unavailability.
- HTTP uses retained bounded four-worker executors (32 request backlog) and stops/shuts down/awaits them during service stop; final shutdown uses `shutdownNow` only if needed.
- Frontend status now reads JSON `serverRunning` rather than treating every HTTP 2xx response as online. Status includes enabled and render state.

## Validation

| Command | Result |
| --- | --- |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build` (both canonical worktrees) | **PASS** — initial parallel clean builds completed successfully. |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain check` (Fabric) | **PASS**. |
| Same command (Forge) | **PASS**. |
| `git diff --check` (Forge) | **PASS**. |

No disposable Minecraft server smoke test was run: the repository has no server harness or test-world setup, and creating a live server would require external Minecraft server assets outside this phase's safe workspace scope.

## Incomplete requirements and risks

The required deterministic Phase 03 lifecycle/privacy/executor/biome regression tests have **not** been added in this pass. The implementation compiles, but those contracts need focused pure-Java test extraction or a Minecraft test harness before this phase may be accepted as complete. Fabric `git diff --check` should also be rerun after the final report addition. Existing Phase 01/02/02B build checks remain passing.

Fabric and Forge are source-aligned for the implemented lifecycle/privacy/thread-safety behavior, but test coverage is incomplete.

PHASE 03 INCOMPLETE — DO NOT CONTINUE
