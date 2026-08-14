# Phase 02 — Configuration validation, render queue bounds, and tile-write concurrency

## Branches inspected

- Fabric `2.1fabric` from `d171c30ee664164745e6b6ed65be0e7a29018f15`.
- Forge `2.1forge` from `bc1cd58de09661bcc197df9fb207a6002586b6e1`, in `/tmp/world-web-map-forge-phase01`.

Both independent implementations of `WebMapConfig`, `RenderJob`, `TileRenderManager`, `TileStorage`, and render commands were inspected before editing.

## Confirmed problems fixed

- Both configs now diagnose invalid integers and reject unsafe values, resetting to documented defaults instead of silently clamping. Validation covers port, fixed 256px tile size, render threads, tiles per tick, render interval, debounce, and non-empty relative tile/web directories.
- Both render managers use the new O(1) `BoundedRenderQueue`; force now affects only re-rendering existing files and cannot bypass capacity.
- Tile keys stay protected while pending and in-flight, releasing only after skipped, failed, or completed worker work. This prevents concurrent duplicate rendering/writing.
- Renderer admission is bounded by a semaphore plus a fixed-size `ThreadPoolExecutor` queue. Snapshot creation stops when no worker/backlog slot is available.
- Tile writes now use `Files.createTempFile` in the destination directory and preserve atomic replacement/failure cleanup.
- `render-area` uses long arithmetic before subtraction/multiplication, rejects oversized/extreme rectangles safely, and iterates with long counters to avoid integer wraparound.
- Added focused JUnit tests for pending capacity and duplicate protection through in-flight completion in both loaders.

## Files changed

Fabric: `build.gradle`, `WebMapCommands.java`, `WebMapConfig.java`, `BoundedRenderQueue.java`, `TileRenderManager.java`, `TileStorage.java`, and `src/test/java/.../BoundedRenderQueueTest.java`.

Forge: the equivalent files under `com/mentality/forgewebmap` plus `build.gradle` and `src/test/java/.../BoundedRenderQueueTest.java`.

Phase 01 uncommitted files remain present and were not altered outside the shared build/metadata scope already reported.

## Validation commands and results

| Command | Result |
| --- | --- |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build` (Fabric) | **PASS** — 10 tasks, including JUnit test execution. |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build` (Forge) | **PASS** — 11 tasks, including JUnit test execution. |
| `rg -n 'queue\\.size\\(|newFixedThreadPool|resolveSibling\\(.*\\.tmp|bypass queue size limit' src/main/java /tmp/world-web-map-forge-phase01/src/main/java` | **PASS for render pipeline** — no obsolete render queue/backlog/temp-file match; the only remaining fixed pools are unrelated HTTP server executors. |
| `git diff --check` and `git -C /tmp/world-web-map-forge-phase01 diff --check` | **PASS** — no whitespace errors. |

No command could not be run.

## Intentionally not touched

- Rendering color/sampling semantics, networking, HTTP endpoints, config key names, user data, tags, releases, and production infrastructure.
- The unrelated fixed HTTP-server executor, which is outside the render pipeline.

## Remaining risks and alignment

Fabric and Forge are behaviorally aligned for this phase's configuration, queue, backlog, duplicate-protection, tile-write, and render-area safety scope. Minecraft-world integration tests remain unimplemented; the new tests deliberately cover only the extracted pure-Java queue policy. The bounded renderer drops a job only if executor rejection occurs after a reserved slot (for example during shutdown), and logs that event rather than allowing unbounded memory growth.
