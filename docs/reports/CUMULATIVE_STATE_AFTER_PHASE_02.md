# Cumulative state after Phase 02

Future canonical worktrees are:

| Loader | Base HEAD | Canonical worktree |
| --- | --- | --- |
| Fabric | `d171c30ee664164745e6b6ed65be0e7a29018f15` | `/home/mentality/Scripts/java-world-web-map` |
| Forge | `bc1cd58de09661bcc197df9fb207a6002586b6e1` | `/home/mentality/Scripts/java-world-web-map-forge-phase02b` |

## Accepted-state presence

| Requirement | Fabric | Forge |
| --- | --- | --- |
| Phase 01 wrapper JAR and final `.gitignore` allow rule | PRESENT | PRESENT |
| Phase 01 CC0 root/loader metadata and narrow compatibility metadata | PRESENT | PRESENT |
| Phase 01 release provenance and Phase 00/01 reports | PRESENT | PRESENT |
| Phase 01 Forge `gradlew.orig` removal | N/A | PRESENT |
| Phase 02 config/queue/admission/temp-file/overflow fixes | PRESENT | PRESENT |
| Phase 02 JUnit test configuration and tests | PRESENT | PRESENT |
| Phase 02B config, force, in-flight, admission, temp, extreme-bound, and priority tests | PRESENT | PRESENT |

## Modified/untracked files by accepted phase

Phase 01: wrapper JAR, `.gitignore`, loader metadata, Forge `LICENSE`, Forge `gradlew.orig` removal, `docs/RELEASE_PROVENANCE.md`, and `docs/reports/PHASE_00_REPORT.md` / `PHASE_01_REPORT.md`.

Phase 02: `WebMapConfig.java`, `TileRenderManager.java`, `TileStorage.java`, `WebMapCommands.java`, `BoundedRenderQueue.java`, `RenderAdmission.java`, `RenderAreaBounds.java`, `build.gradle`, and test sources in both loader packages; `docs/reports/PHASE_02_REPORT.md`.

Phase 02B: queue priority behavior and expanded tests; `docs/reports/PHASE_02B_REPORT.md`.

Files overlapping Phase 02 and 02B are `BoundedRenderQueue.java`, `TileRenderManager.java`, `build.gradle`, and the test source trees. The only non-accepted untracked paths are generated `logs/` directories from test/build execution; they are not part of the cumulative source state.

Every later phase must start from the canonical cumulative working state produced by the previous phase. Never recreate a loader worktree from origin/base and then continue as if prior uncommitted phases were present. If a fresh worktree is unavoidable, first reconstruct and verify all accepted prior-phase changes before editing.
