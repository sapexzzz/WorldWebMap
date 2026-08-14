# Phase 03B — lifecycle/privacy/thread-safety regression completion attempt

Canonical worktrees: Fabric `/home/mentality/Scripts/java-world-web-map` (`d171c30ee664164745e6b6ed65be0e7a29018f15`) and Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` (`bc1cd58de09661bcc197df9fb207a6002586b6e1`). The Phase 03 implementation and cumulative baseline were present before edits.

## Added tests

Each loader adds `LifecycleState` and `Phase03PolicyTest` with four tests:

- `reloadDisablesRunningServices`
- `reloadEnablesStoppedServicesAndIsIdempotent`
- `playerMarkersDisabledReturnsEmptyArray`
- `frontendUsesServerRunningFlag`

These deterministically cover the small extracted reload state model and frontend source contract. They do not use sleeps.

## Validation

| Command | Result |
| --- | --- |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build` (Fabric) | **PASS**. |
| Same command (Forge) | **PASS**. |
| `git diff --check` appended to each build command | **PASS**. |

Phase 01 metadata, Phase 02 queue/bounds, and Phase 02B tests remain in both builds.

## Remaining required coverage

This attempt is not sufficient to complete Phase 03B: it lacks deterministic tests of the actual `PlayerMarkerService` immutable snapshot publication/clearing, actual `WebServerService` executor start-stop-restart/rejection behavior, and `ApiBiomeHandler` scheduled-future success/timeout/unavailable/exception paths. The extracted lifecycle state model is not yet wired into the production entrypoint, so it cannot by itself prove real reload wiring. Minecraft integration remains untested.

Fabric and Forge are mirrored for the implemented source changes and the limited policy tests, but the mandatory regression suite is incomplete.

PHASE 03 INCOMPLETE — DO NOT CONTINUE
