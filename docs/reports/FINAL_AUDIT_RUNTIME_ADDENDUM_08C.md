# Final audit runtime addendum — Phase 08C

## 1–3. Canonical worktrees and carried-forward fixes

Fabric remains `2.1fabric` at `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge remains `2.1forge` at `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Cumulative dirty state was preserved. Phase 08B found and fixed the biome handler installation ordering and confirmed unsafe forced terrain generation. Both loaders now pass `loadChunks=false` for manual, area, fullrender, and auto-render jobs; the low-level opt-in builder path has no production caller. Both have named contracts: `renderDoesNotGenerateUnloadedChunk`, `renderAreaDoesNotGenerateUnloadedChunks`, `fullRenderDoesNotGenerateUnloadedChunks`, `autoRenderDoesNotGenerateUnloadedChunks`, and `unloadedChunkIsSkippedWithoutForcedGeneration`.

## 4–6. Exact artifacts and Forge metadata

Fabric tested JAR: `build/libs/fabricwebmap-0.2.1.jar`, SHA-256 `428243d3161e74f44fa8fd5ca4e63a4192d80609894617af6f988a9a3a60c84f`. Forge tested JAR: `build/libs/forgewebmap-0.2.1.jar`, SHA-256 `730f9bc0f9582b09c65625f64a59a8d55b72aca08ccc73e27dee8c0246ca4aab`.

Forge 47.2.0 live launches proved that identical-bound Maven ranges are invalid. Final metadata is `loaderVersion="[47,)"`, Forge dependency `[47.2.0,47.2.1)`, and Minecraft dependency `[1.20.1,1.20.2)`. The final Forge artifact validator accepts the JAR.

## 7. Forge startup and APIs

Official Forge 47.2.0 disposable server at `/tmp/world-web-map-runtime.0ROAUz/forge-server`, Java 17, loopback `127.0.0.1:18125`: PASS. It loaded `forgewebmap 0.2.1`, reached `Done`, started renderer/web service, and `/api/status`, `/api/config`, `/api/players`, and `/api/biome?dim=overworld&x=0&z=0` all returned valid JSON with HTTP 200. Biome returned `minecraft:plains` and status reported `serverRunning:true`.

## 8–15. Live-scope results

Fabric forced-chunk rerun after the no-generation change: PASS (Phase 08B target `(30,30)` created no target region and no stall). Forge forced-chunk rerun, Forge lifecycle/port reload, both-loader Nether/End, post-fix performance/fullrender/stoprender, and ten-cycle executor leak smoke: UNVERIFIED in this addendum. The successfully detached Forge process had no controllable console input, so the required server commands could not be issued without creating a new harness; this was not substituted with an unsafe claim. Fabric’s earlier tiny fullrender produced multi-second warnings, which remains a performance concern.

Fabric PNG integrity in Phase 08B: valid 256x256 PNGs, decoder PASS, no `.tmp`. Forge rendered-tile integrity is UNVERIFIED because no render command was issued.

## 16–19. Runtime defects, deterministic validation, and final metadata

New runtime defects fixed: biome handler replacement ordering; invalid Forge loader/Forge/Minecraft version-range syntax; and terrain-generating render jobs. Final Java 17 `clean check build`: Fabric PASS, 104 tests; Forge PASS, 103 tests. Final Forge JAR metadata is version `0.2.1`, CC0-1.0, loader `[47,)`, Forge `[47.2.0,47.2.1)`, Minecraft `[1.20.1,1.20.2)`.

## 20–24. Verdict

Fabric runtime verdict: PARTIAL PASS; prior forced no-generation evidence is good, but performance and remaining smoke are incomplete. Forge runtime verdict: STARTUP/API PASS; lifecycle/render/forced-generation smoke is incomplete. Remote CI remains intentionally unverified and no push/tag/release occurred.

Remaining blockers: complete controllable-console Forge lifecycle and forced-chunk experiment; rerun Fabric forced-chunk in a fresh final harness; both loader Nether/End, performance, stoprender, and executor-cycle smoke; final validators/diff checks after all report updates. Historical reports are retained unchanged.

RUNTIME VALIDATION FAILED — RELEASE BLOCKED
