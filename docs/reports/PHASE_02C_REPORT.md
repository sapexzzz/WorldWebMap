# Phase 02C — cumulative state consolidation

## Canonical worktrees and base SHAs

- Fabric: `/home/mentality/Scripts/java-world-web-map`, `2.1fabric`, base `d171c30ee664164745e6b6ed65be0e7a29018f15`.
- Forge: `/home/mentality/Scripts/java-world-web-map-forge-phase02b`, `2.1forge`, base `bc1cd58de09661bcc197df9fb207a6002586b6e1`.

## Consolidation result

Fabric already contained all accepted Phase 01, 02, and 02B changes. The recreated Forge worktree was missing accepted Phase 01 artifacts; this pass restored its CC0 root `LICENSE`, CC0/narrow `mods.toml` metadata, wrapper-JAR tracking, removal of `gradlew.orig`, release provenance document, and prior reports. Its accepted Phase 02/02B queue, admission, configuration, storage, rectangle, Gradle-test, and test-source changes were retained.

Phase 01: **fully present** in Fabric and Forge. Phase 02: **fully present** in Fabric and Forge. Phase 02B: **fully present** in Fabric and Forge.

## Files changed during consolidation

Forge only: `.gitignore`, `LICENSE`, `gradle/wrapper/gradle-wrapper.jar`, `gradlew.orig` (removed), `src/main/resources/META-INF/mods.toml`, `docs/RELEASE_PROVENANCE.md`, and copied prior reports under `docs/reports/`. Fabric only gained this manifest and this Phase 02C report.

## Validation

| Command | Result |
| --- | --- |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build` (Fabric canonical worktree) | **PASS** — tests and build completed. |
| Same command (Forge canonical worktree) | **PASS** — tests and build completed. |
| `git diff --check` (Fabric) | **PASS**. |
| `git diff --check` (Forge) | **PASS**. |
| `unzip -p build/libs/fabricwebmap-0.2.1.jar fabric.mod.json` | **PASS** — version 0.2.1, CC0-1.0, Fabric API `>=0.92.2+1.20.1`, Minecraft `~1.20.1`. |
| `unzip -p build/libs/forgewebmap-0.2.1.jar META-INF/mods.toml` | **PASS** — version 0.2.1, CC0-1.0, Forge `[47.2.0,47.2.0]`, Minecraft `[1.20.1,1.20.1]`. |

Reports are stored under `docs/reports/`; no phase report exists in either repository root. No validation command was skipped.

## Remaining risks

Generated untracked `logs/` directories are not source changes. The loader lines remain separate Git histories and must continue to be updated separately. Phase 02 test coverage is pure-Java policy/filesystem focused; Minecraft server integration remains a later concern.

Both loader states are safe to use as the cumulative baseline for Phase 03.

CUMULATIVE BASELINE READY FOR PHASE 03
