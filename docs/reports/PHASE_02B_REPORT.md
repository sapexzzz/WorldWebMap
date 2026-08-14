# Phase 02B corrective completion

## Inspected worktrees

- Fabric: `/home/mentality/Scripts/java-world-web-map`, branch `2.1fabric`, base commit `d171c30ee664164745e6b6ed65be0e7a29018f15`.
- Forge: `/home/mentality/Scripts/java-world-web-map-forge-phase02b`, branch `2.1forge`, base commit `bc1cd58de09661bcc197df9fb207a6002586b6e1`.

The former temporary Forge worktree had been removed by the environment before this pass; this fresh dedicated worktree was recreated from the same Forge commit. No history was rewritten.

## Corrected contracts and tests

- Config recovery is explicit, not silent: invalid values produce an error with the key/range message and reset to named defaults. Tests cover low/high ports, zero/non-256 tile size, non-positive/excessive threads, non-positive tiles per tick, blank tile/web directories, and malformed integer input.
- `forceRenderDoesNotBypassQueueCapacity` proves the one globally bounded queue rejects a third force-named job once full; no secondary queue exists.
- In-flight duplicate tests prove a key remains rejected after polling and can be re-enqueued only after `complete`, the shared cleanup used for success, render failure, and executor rejection.
- `RenderAdmissionTest` deterministically proves two slots (one worker plus one backlog slot per worker), backpressure when full, and permit return for success/failure/rejection paths.
- Temp-file tests prove independent destination-directory temporary files are unique and cleanable. Production uses `Files.createTempFile` followed by atomic replace with the existing fallback and failure cleanup.
- `RenderAreaBoundsTest` covers integer extremes, max coordinates, reversed bounds, exactly 10,000 tiles, over-limit bounds, and negative coordinates without iteration.
- Priority is now meaningful: the single globally bounded queue has three internal bounded priority lanes; HIGH polls before NORMAL before LOW, while deduplication applies across lanes.

Tests added: Fabric 9 test methods across `BoundedRenderQueueTest`, `RenderAdmissionTest`, `RenderAreaBoundsTest`, `WebMapConfigTest`, and `TileStorageTempFileTest`; Forge 7 test methods across the equivalent five test classes.

## Files changed in this corrective pass

Fabric: `BoundedRenderQueue.java`, `TileRenderManager.java`, `RenderAdmission.java`, `RenderAreaBounds.java`, `TileStorage.java`, and the five listed test classes.

Forge: restored equivalent Phase 02 queue/config/manager/storage/rectangle policy classes and the equivalent test classes, plus its required wrapper JAR to make the fresh clean checkout buildable.

## Validation

| Command | Result |
| --- | --- |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build` (Fabric) | **PASS** — tests and build succeeded. |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build` (Forge) | **PASS** — tests and build succeeded. |
| `git diff --check` (both worktrees) | **PASS**. |

No required command was skipped.

## Remaining risks

The new tests are pure-Java policy/filesystem tests; a live Minecraft server integration run remains outside this corrective pass. The bounded renderer intentionally applies backpressure instead of accumulating snapshots. Existing Phase 01 changes must remain included when the Forge worktree is later committed, since this fresh environment worktree began from the original Forge branch.

## Completion

**Phase 02 can now be considered COMPLETE** for the requested reliability contracts: configuration recovery is explicit, capacity and in-flight protection are tested, renderer admission is bounded, priority is deterministic, overflow is guarded, and both loaders pass Java 17 `clean check build`.
