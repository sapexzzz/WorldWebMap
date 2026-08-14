# Phase 03C — production-wiring regression completion

Canonical worktrees: Fabric `/home/mentality/Scripts/java-world-web-map` at `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` at `bc1cd58de09661bcc197df9fb207a6002586b6e1`.

The cumulative baseline and Phase 03 implementation were present before this pass. Initial statuses contained the accepted prior uncommitted changes plus Phase 03 source edits.

## Dead abstraction removal

`LifecycleState` and `Phase03PolicyTest` were removed from both loaders. They were test-only, not used by production entrypoints, and therefore could not prove real reload behavior.

## Production state confirmed

Phase 03 production source remains present: reload retains the server and restores it after HTTP recreation; player data uses a volatile immutable snapshot; markers clear on disable; biome lookup uses server-thread submission with a bounded wait; HTTP retains a bounded owned executor and shuts it down; status frontend reads JSON `serverRunning`.

## Incomplete acceptance bar

This pass did not add the required production-wiring tests for real entrypoint lifecycle delegation, real `PlayerMarkerService` publication/handler serialization, production HTTP executor ownership/rejection, real `ApiBiomeHandler` scheduler outcomes, or backend status serialization. No minimal production-used test seam has been implemented for those contracts. Therefore the Phase 03C acceptance bar is not met.

No build was rerun after removing the dead files; the preceding Phase 03B clean builds were successful, but that does not satisfy Phase 03C's required final validation.

PHASE 03 INCOMPLETE — DO NOT CONTINUE
