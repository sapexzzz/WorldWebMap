# Phase 03D — production lifecycle/privacy/HTTP proof

Canonical worktrees: Fabric `/home/mentality/Scripts/java-world-web-map` (`d171c30ee664164745e6b6ed65be0e7a29018f15`) and Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` (`bc1cd58de09661bcc197df9fb207a6002586b6e1`).

## Production seams introduced and exercised

- `PlayerMarkerService.publish(List<PlayerInfo>)` is the real server-thread publication boundary used by `refresh`; tests exercise immutable publication, immediate disable clearing, and no stale resurrection.
- `HttpExecutorFactory.create()` is now used by real `WebServerService`; latch-based tests exercise the exact worker/backlog policy, 32-item bound, and `RejectedExecutionException` on saturation.

Both loaders passed `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build` and `git diff --check`.

## Remaining acceptance gaps

The real entrypoint lifecycle/reload delegation, actual `WebServerService` stop/restart ownership, actual `ApiBiomeHandler` scheduler success/timeout/no-server/error responses, real API-player JSON serialization, backend status serialization, and chunk accumulation tests are still not covered by deterministic production-wiring tests. Therefore the non-negotiable acceptance rule is not yet met, despite successful builds and the new real snapshot/executor tests.

Phase 01 metadata and Phase 02/02B tests remain present through the successful full builds. Fabric and Forge remain aligned for the implemented seams.

PHASE 03 INCOMPLETE — DO NOT CONTINUE
