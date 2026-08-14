# Phase 03F — API handler behavior and serialization proof

Canonical worktrees: Fabric `/home/mentality/Scripts/java-world-web-map` (`2.1fabric`, `d171c30ee664164745e6b6ed65be0e7a29018f15`) and Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` (`2.1forge`, `bc1cd58de09661bcc197df9fb207a6002586b6e1`). Both retained their cumulative uncommitted status recorded in Phase 03E; no reset/recreation occurred.

## Handler proof and production seams

- Real `ApiPlayersHandler` is exercised through a reusable fake `HttpExchange`. It returns `[]` after marker disable, serializes the immutable published snapshot, and safely escapes quote, backslash, and newline names.
- A production-used `ApiBiomeHandler.BiomeLookup` future seam drives the handler's real scheduling result/timeout/error path. The default implementation still schedules against the installed Minecraft server. Tests cover success, deterministic timeout, unavailable server, exceptional task, invalid dimension, and a replacement handler with server-ready context.
- Unknown dimensions previously fell through `DimensionUtil.fromWebName` to Overworld. The handler now returns JSON `400 Unknown dimension` without scheduling a lookup.
- `ApiStatusHandler` now uses production web/server availability suppliers, installed by `WebServerService`; `serverRunning` is true only when both are true. Tests cover enabled/running, disabled/stopped, idle renderer, and unavailable server.
- `JsonUtil` list serialization had a real empty-list defect (`]`); it now emits valid `[]` in both loaders.
- Frontend test verifies `app.js` reads `data.serverRunning` and makes failures offline. HTTP 200 with `serverRunning=false` therefore does not indicate online.

All handler JSON responses tested use UTF-8 `application/json; charset=utf-8`, explicit status codes, and no exception details. `renderedTiles` continues to increment only after successful renderer completion; no telemetry redesign was needed.

## Exact contract tests

Each loader executes `playerMarkersDisabledReturnsEmptyArray`, `playerMarkersEnabledSerializesSnapshot`, `playerJsonEscapingIsValid`, `biomeLookupSuccessReturnsPayload`, `biomeLookupTimeoutReturns503`, `biomeLookupWithoutServerReturns503`, `biomeLookupExceptionReturnsControlledFailure`, `biomeLookupUnknownDimensionIsRejected`, `biomeHandlerWorksAfterWebServiceRecreation`, `statusEnabledAndRunning`, `statusDisabledAndStopped`, `statusReflectsStoppedRenderer`, `statusReflectsUnavailableServer`, and `frontendUsesServerRunningFlag`.

## Validation

- Fabric and Forge Java 17 `clean check build`: **PASS**.
- Fabric and Forge `git diff --check`: **PASS**.
- Total tests: **39 Fabric**, **38 Forge**.
- Phase 03D snapshot/executor and Phase 03E lifecycle/ownership/chunk tests remain included in the successful complete suites.
- Built JAR metadata: version `0.2.1`, license `CC0-1.0`, and loader/Minecraft constraints unchanged (Fabric loader `>=0.15.11`, Minecraft `~1.20.1`; Forge `47.2.0`, Minecraft `1.20.1`).

Files changed include both loaders' biome/status/web/JSON production classes, shared handler test exchanges and API tests, plus this report. Fabric and Forge implementations are behaviorally aligned.

Remaining risk is limited to true live-Minecraft integration (real server scheduling and biome registry/world data), which is outside these deterministic unit tests.

PHASE 03 COMPLETE — READY FOR PHASE 04
