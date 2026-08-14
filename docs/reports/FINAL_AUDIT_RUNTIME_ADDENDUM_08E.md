# Final audit runtime addendum — Phase 08E

## 1. Canonical worktrees + SHAs

Fabric `/home/mentality/Scripts/java-world-web-map`: `2.1fabric`, `d171c30ee664164745e6b6ed65be0e7a29018f15`. Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b`: `2.1forge`, `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Both retained their cumulative dirty state; no reset, commit, push, tag, or release occurred.

## 2. Final pre-runtime test totals

Fabric 104 tests PASS; Forge 103 tests PASS, using Java 17 `clean check build` before server startup.

## 3. Fabric final JAR SHA-256

Live matrix artifact: `build/libs/fabricwebmap-0.2.1.jar`, SHA-256 `d3cd512cc387406aa2351920cdaf07d1acaba0ab9bfec8be9867dade7b123a49`.

## 4. Forge final JAR SHA-256

Live matrix artifact: `build/libs/forgewebmap-0.2.1.jar`, SHA-256 `d148c8c2e8882102a61f1fbb5904d7cdc58ab57dd24a02066ca0f284bbc7929c`.

## 5. Fresh runtime harness paths/control method

Fresh disposable directories were `/tmp/world-web-map-runtime-08e/fabric-server` and `/tmp/world-web-map-runtime-08e/forge-server`. Fabric used the local 1.20.1 server launcher; Forge used an official 47.2.0 installer. Both worlds were new, web services bound only to `127.0.0.1`, and foreground Java processes were controlled through named `tmux` consoles.

## 6. Fabric startup/API final-JAR result

PASS. Reached `Done`; initialized World Web Map and loopback service. Status, config, players, and controlled overworld biome endpoints each returned HTTP 200 valid JSON.

## 7. Forge startup/API final-JAR result

PASS. Official Forge 47.2.0 reached `Done`, initialized the mod/service, and all four endpoints returned HTTP 200 valid JSON.

## 8. Fabric forced-chunk disk experiment

PASS — **NO NEW CHUNK GENERATION OBSERVED**. After normal spawn baseline, target tile `(30,30)` was rendered without travel or preload. The target `r.15.15.mca` was absent after clean shutdown; only normal spawn-region files changed during server restart. Render completed as an empty/skip result without a multi-second generation stall.

## 9. Forge forced-chunk disk experiment

PASS — **NO NEW CHUNK GENERATION OBSERVED**. The same fresh-world target `(30,30)` left no `r.15.15.mca`; only ordinary spawn-region restart changes occurred.

## 10. Fabric fullrender status result

PASS. Radius 1 immediately reported `fullRenderActive:true`, `fullRenderRemaining:9`; it remained active through processing and eventually returned inactive with queue zero. A repeat completed consistently.

## 11. Forge fullrender status result

PASS. Radius 1 reported active/remaining 9 immediately, then active with queued jobs (queue 7) after production, and inactive only after drain. A repeat completed consistently.

## 12. Fabric stoprender result

PASS. A live radius-1 plan was cancelled with seven pending jobs removed; status reached inactive/queue zero. A following normal render succeeded and no temporary/corrupt file appeared.

## 13. Forge stoprender result

PASS. The corresponding cancellation cleared seven pending jobs; a following normal render succeeded with no corruption.

## 14. Fabric performance smoke

PASS. Idle, first radius-1, and repeat radius-1 checks were console-responsive and produced no `Can't keep up!` warning on final JAR.

## 15. Forge performance smoke

PASS. Idle plus two final-JAR radius-1 checks were responsive and produced no `Can't keep up!` warning. This supersedes the pre-fix 08D stalls.

## 16. Fabric Nether result

PASS. A normally force-loaded disposable Nether chunk triggered auto rendering. Nether PNGs decoded as 256×256; inspected sample had 28 colors, so it is not a uniform bedrock roof. No `.tmp` remained.

## 17. Forge Nether result

PASS. Same result: a 256×256 decoded PNG with 28 colors, not roof-only, and no `.tmp`.

## 18. Fabric End result

PASS. Minimal force-loaded disposable End chunks auto-rendered 256×256 decodable PNGs with no temporary leftovers.

## 19. Forge End result

PASS. Minimal disposable End auto-render produced valid 256×256 PNGs with no temporary leftovers.

## 20. Fabric 10-cycle executor result

PASS. Ten consecutive port changes each closed the old listener and served `/api/status` on the new listener. Valid process-thread measurement was 61 before and 59 after; no web-worker accumulation or stale listener appeared.

## 21. Forge 10-cycle executor result

PASS. Ten equivalent reloads passed. Process threads were 69 before and 66 after; no monotonic executor growth or stale listener appeared.

## 22. Tile integrity summary both

PASS. Fabric: 12 PNGs; Forge: 12 PNGs. Every file decoded as PNG at 256×256; `.tmp` count 0 and corrupt/quarantine count 0.

## 23. Forced-generation source audit

**NO PRODUCTION RENDER PATH SETS loadChunks=true.** Manual, render-area, auto, and final `FullRenderPlan` callers pass false. The low-level builder conditional remains dormant unless explicitly called with true and has no production caller.

## 24. Post-cycle lifecycle sanity

PASS for both: disabled markers returned exactly `[]`; disabled service closed its current port; re-enable restored status and biome endpoints on the current port.

## 25. Runtime-discovered fixes, if any

None in Phase 08E. It verified the Phase 08D fullrender-lifetime, chunk-local sampling, and no-forced-fullrender fixes.

## 26. Final deterministic Fabric build/test count

PASS: Java 17 `clean check build`, 104 tests; post-runtime SHA unchanged at `d3cd512cc387406aa2351920cdaf07d1acaba0ab9bfec8be9867dade7b123a49`.

## 27. Final deterministic Forge build/test count

PASS: Java 17 `clean check build`, 103 tests. Post-runtime SHA was `96b01e290d298d0507ccaf3ec5544daa0ca1b9461c02da13292c69b76076feb2`, differing from the live SHA despite no source change; this is Forge artifact non-reproducibility and is recorded rather than mixing runtime evidence.

## 28. Artifact/docs/YAML validation

PASS: both artifact validators and docs validators passed; all Dependabot and workflow YAML files parsed.

## 29. Final JAR metadata

PASS. Fabric: CC0-1.0, loader `>=0.15.11`, Minecraft `~1.20.1`. Forge: CC0-1.0, loader `[47,)`, Forge `[47.2.0,47.2.1)`, Minecraft `[1.20.1,1.20.2)`.

## 30. Final post-report `git diff --check` both

PASS, run after this report and the minimal final-audit update.

## 31. Fabric runtime verdict

PASS.

## 32. Forge runtime verdict

PASS, with the Forge build-SHA reproducibility difference documented in section 27.

## 33. Updated overall release-readiness verdict

Local runtime validation is complete. Remote CI and branch-protection evidence remain outside this local no-push task; the existing CDN deployment limitation also remains documented.

## 34. Exact remaining limitations/blockers

No local runtime blocker remains. Before an actual release, push intentionally, verify remote CI, review branch protection, and resolve or accept the documented CDN and Forge artifact-reproducibility limitations.

RUNTIME VALIDATION PASSED WITH DOCUMENTED LIMITATIONS — READY FOR REMOTE CI/RELEASE PROCESS
