# Final audit runtime addendum — Phase 08D

## 1. Canonical worktrees + SHAs

Fabric: `/home/mentality/Scripts/java-world-web-map`, branch `2.1fabric`, HEAD `d171c30ee664164745e6b6ed65be0e7a29018f15`. Forge: `/home/mentality/Scripts/java-world-web-map-forge-phase02b`, branch `2.1forge`, HEAD `bc1cd58de09661bcc197df9fb207a6002586b6e1`.

## 2. Initial git status

Both canonical worktrees began intentionally dirty with the cumulative Phase 08 state; no reset, commit, push, tag, or publication was performed.

## 3. Runtime harness/control method

Disposable servers were created below `/tmp/world-web-map-runtime-final/`, with separate fresh Fabric and official Forge 47.2.0 installations. Each ran in a named `tmux` foreground console, accepting deterministic `webmap` and `stop` commands. Web services bound only to `127.0.0.1`.

## 4. Fabric JAR SHA-256 tested

The first live pass used fresh `fabricwebmap-0.2.1.jar` SHA-256 `428243d3161e74f44fa8fd5ca4e63a4192d80609894617af6f988a9a3a60c84f`. The final rebuilt artifact after runtime fixes is `d3cd512cc387406aa2351920cdaf07d1acaba0ab9bfec8be9867dade7b123a49`; it was not substituted into a completed live matrix.

## 5. Forge JAR SHA-256 tested

The first live pass used fresh `forgewebmap-0.2.1.jar` SHA-256 `ba892b0d708d475788aa154b5ac166c233366742f6e45e0f46a03857d960ea4e`. The final rebuilt artifact after runtime fixes is `8d10f8c07ebb206925373000027026f88e89259a4336f1cfecd4ec2675830344`; it was not substituted into a completed live matrix.

## 6. Fabric startup/API/lifecycle matrix

PASS: ready state, initialization, loopback web service, and HTTP 200 valid JSON for status/config/players/biome. PASS: markers-disabled reload returned exactly `[]`; disabling closed the old port; re-enable and port move `18123` to `18124` succeeded; biome remained available. Fullrender status initially failed (see section 20), then the corrected code was deterministically rebuilt but not live rerun.

## 7. Forge startup/API/lifecycle matrix

PASS: official Forge 47.2.0 loaded the mod and reached `Done`; all four APIs returned HTTP 200 valid JSON. PASS: markers-disabled reload returned `[]`; disabling closed the port; re-enable and port move `18126` to `18127` succeeded. Fullrender status initially failed (see section 20), then the corrected code was deterministically rebuilt but not live rerun.

## 8. Fabric forced-chunk final result

UNVERIFIED on the final artifact. Phase 08D source audit found a fullrender caller still requesting chunk loads, so the earlier live observation cannot satisfy this final requirement.

## 9. Forge forced-chunk final result

UNVERIFIED on the final artifact for the same fullrender caller defect.

## 10. Fabric Nether result

UNVERIFIED: no final-jar controllable Nether render was run after the discovered production-path defect.

## 11. Forge Nether result

UNVERIFIED: no final-jar controllable Nether render was run after the discovered production-path defect.

## 12. Fabric End result

UNVERIFIED: deterministic End coverage remains green, but no final-jar live End tile was run.

## 13. Forge End result

UNVERIFIED: deterministic End coverage remains green, but no final-jar live End tile was run.

## 14. Fabric performance smoke

WARNING/UNRESOLVED on the pre-fix artifact: a radius-1 run logged a 2.530s `Can't keep up!` warning. The final chunk-local sampling change requires a new runtime smoke.

## 15. Forge performance smoke

BLOCKER on the pre-fix artifact: radius-1 rendering logged repeated 8.637s and 34.092s `Can't keep up!` stalls. The final chunk-local sampling change requires a complete rerun before classification can change.

## 16. Fabric executor 10-cycle result

UNVERIFIED: the ten-cycle leak smoke was not completed after the runtime blocker was found.

## 17. Forge executor 10-cycle result

UNVERIFIED: the ten-cycle leak smoke was not completed after the runtime blocker was found.

## 18. Tile integrity summary

Pre-fix Fabric: two PNGs found, valid 256x256 files, no `.tmp`; Forge: two PNGs found, valid 256x256 files, no `.tmp`. This is not final-artifact evidence.

## 19. Source audit for forced generation

Before the final correction, both `FullRenderPlan.peek()` production paths passed `loadChunks=true`. All manual/area/auto callers used false. Final code now passes false in both plans; deterministic no-generation contracts pass. A final live fresh-world experiment remains mandatory.

## 20. Runtime-discovered fixes

Mirrored both loaders: retain fullrender status until queued/active work drains; sample height and block state from the already-resolved loaded `LevelChunk`; change `FullRenderPlan` to `loadChunks=false`. No unrelated changes were made.

## 21. Post-runtime Fabric test total/build result

PASS: Java 17 `clean check build`, 104 tests.

## 22. Post-runtime Forge test total/build result

PASS: Java 17 `clean check build`, 103 tests.

## 23. Artifact/docs/YAML validation

PASS: both artifact validators and documentation validators. YAML parsed successfully for Dependabot and both workflow files in each worktree.

## 24. Final JAR metadata

PASS: Fabric metadata is version 0.2.1, CC0-1.0, loader `>=0.15.11`, Minecraft `~1.20.1`. Forge is version 0.2.1, CC0-1.0, loader `[47,)`, Forge `[47.2.0,47.2.1)`, Minecraft `[1.20.1,1.20.2)`.

## 25. Final post-report git diff --check both

PASS: Fabric and Forge `git diff --check` completed after this report was written.

## 26. Fabric runtime verdict

FAILED / INCOMPLETE: core lifecycle APIs passed, but final-artifact forced-generation, Nether/End, executor, tile, and performance evidence is incomplete.

## 27. Forge runtime verdict

FAILED / INCOMPLETE: core lifecycle APIs passed, but a repeated severe performance stall was observed before the final fix and final-artifact runtime evidence is incomplete.

## 28. Updated overall release-readiness verdict

The release remains blocked. This phase found and corrected real production-path defects, but it did not complete the required final-JAR runtime rerun.

## 29. Exact remaining limitations/blockers

Run the complete controllable fresh-world matrix using the final SHA JARs: forced-chunk disk comparison, both Nether/End tiles, performance repeats, stoprender, ten port cycles, and final tile integrity. Remote CI and branch-protection evidence also remain outside this local task.

RUNTIME VALIDATION FAILED — RELEASE BLOCKED
