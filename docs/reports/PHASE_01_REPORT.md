# Phase 01 — Build reproducibility, release provenance, metadata, and license

Audit/implementation date: 2026-08-12 (Europe/Paris).

## Branches and commits inspected

- Fabric: local `2.1fabric`, starting at `d171c30ee664164745e6b6ed65be0e7a29018f15`.
- Forge: local `2.1forge` worktree at `/tmp/world-web-map-forge-phase01`, created from `origin/2.1forge` at `bc1cd58de09661bcc197df9fb207a6002586b6e1`.

The branches remain separate histories. No merge, rebase, tag movement, release operation, artifact upload, or runtime-code change was made.

## Preliminary housekeeping

`docs/reports/PHASE_00_REPORT.md` was already present at the required report location when Phase 01 began. There is no root-level `PHASE_00_REPORT.md` and no duplicate was created.

## Confirmed problems fixed

### Gradle wrappers

- Restored `gradle/wrapper/gradle-wrapper.jar` in both loader trees. The restored standard wrapper JAR has SHA-256 `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7` and contains `org/gradle/wrapper/GradleWrapperMain.class`.
- Retained the existing standard `gradlew` and `gradlew.bat` scripts; both reference `GradleWrapperMain`.
- Moved the wrapper-JAR allow rule to the end of both `.gitignore` files so the archive-wide `*.jar` rule cannot hide the required JAR.
- Removed Forge `gradlew.orig`. Static comparison confirmed it was a non-wrapper launcher that used `lib/gradle-launcher-8.1.1.jar`, `GradleMain`, and a parent-directory `APP_HOME`; it is obsolete and should not be retained beside the real wrapper.

### License and metadata

The only affirmative license-intent evidence is Fabric commit `d171c30` (`Create LICENSE`, authored by the project author), which added a root CC0 1.0 Universal license. Older Fabric and Forge metadata said MIT, and Forge had no root license. The Phase 01 decision therefore follows that explicit repository license rather than inventing a different preference:

- Fabric `fabric.mod.json`: `license` changed from `MIT` to `CC0-1.0`.
- Forge: added the same root `LICENSE` text as the Fabric CC0 1.0 license; `mods.toml` now declares `CC0-1.0`.
- Fabric API metadata now states the build-known minimum, `>=0.92.2+1.20.1`, instead of `*`.
- Forge loader and Forge dependency metadata are exact `[47.2.0,47.2.0]`; Minecraft metadata is exact `[1.20.1,1.20.1]`. This avoids claiming untested broader 47.x or Minecraft 1.20.x support.

### Release provenance

Added `docs/RELEASE_PROVENANCE.md` to both loader trees. It records that both historical 2.1 tags resolve to Forge 0.2.0 source while published 2.1 assets are named loader-specific 0.2.1 JARs, and documents the safe procedure for future loader-specific patch tags and artifact builds. Existing tags and release records were not modified.

## Files changed

Fabric (`2.1fabric`):

- `.gitignore`
- `gradle/wrapper/gradle-wrapper.jar`
- `src/main/resources/fabric.mod.json`
- `docs/RELEASE_PROVENANCE.md`
- `docs/reports/PHASE_01_REPORT.md`

Forge (`2.1forge` worktree):

- `.gitignore`
- `LICENSE`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew.orig` (removed)
- `src/main/resources/META-INF/mods.toml`
- `docs/RELEASE_PROVENANCE.md`

## Validation commands and results

All commands below used Java 17 by prepending `/usr/lib/jvm/java-17-openjdk/bin` to `PATH`.

| Command | Result |
| --- | --- |
| `jar tf gradle/wrapper/gradle-wrapper.jar \| rg 'org/gradle/wrapper/GradleWrapperMain.class'` | **PASS** — Fabric wrapper main class present. |
| `jar tf /tmp/world-web-map-forge-phase01/gradle/wrapper/gradle-wrapper.jar \| rg 'org/gradle/wrapper/GradleWrapperMain.class'` | **PASS** — Forge wrapper main class present. |
| `rg -n 'GradleWrapperMain' gradlew gradlew.bat` | **PASS** — Fabric POSIX and Windows scripts are structurally valid wrapper launchers. |
| `rg -n 'GradleWrapperMain' /tmp/world-web-map-forge-phase01/gradlew /tmp/world-web-map-forge-phase01/gradlew.bat` | **PASS** — Forge POSIX and Windows scripts are structurally valid wrapper launchers. |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --version` (Fabric) | **PASS** — Gradle 8.8 runs on JVM 17.0.20. |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --version` (Forge) | **PASS** — Gradle 8.1.1 runs on JVM 17.0.20. |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean build` (Fabric) | **PASS** — downloaded Gradle 8.8 and completed `BUILD SUCCESSFUL` (8 tasks). |
| First `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean build` (Forge) | **FAIL — transient environmental download failure** — wrapper started, but its single Gradle 8.1.1 download attempt ended with `java.net.SocketException: Unexpected end of file from server`; this is not the former missing-wrapper-JAR defect. |
| Retried `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean build` (Forge) | **PASS** — Gradle 8.1.1 completed `BUILD SUCCESSFUL` (9 tasks). |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" /tmp/gradle-distributions/gradle-8.8/bin/gradle --no-daemon --console=plain clean build` | **PASS** — Fabric clean build with the exact configured Gradle 8.8. |
| `PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" /tmp/gradle-distributions/gradle-8.1.1/bin/gradle --no-daemon --console=plain clean build --stacktrace` | **PASS** — Forge clean build with the exact configured Gradle 8.1.1; output JARs were produced. |
| `unzip -p build/libs/fabricwebmap-0.2.1.jar fabric.mod.json` | **PASS** — version `0.2.1`, license `CC0-1.0`, Fabric API minimum, and Minecraft `~1.20.1` rendered correctly. |
| `unzip -p /tmp/world-web-map-forge-phase01/build/libs/forgewebmap-0.2.1.jar META-INF/mods.toml` | **PASS** — version `0.2.1`, license `CC0-1.0`, Forge `47.2.0`, and Minecraft `1.20.1` rendered correctly. |
| `curl -L -I --max-time 30 https://services.gradle.org/distributions/gradle-8.8-bin.zip` and the equivalent 8.1.1 command | **PASS** — official endpoint redirects to the expected Gradle release assets; the Forge wrapper's EOF remains an intermittent Java/HTTP transfer condition in this environment. |
| `git diff --check` (Fabric and Forge worktrees) | **PASS** — no whitespace errors. |

No validation command was skipped. One initial Forge wrapper download failed transiently as recorded above; the required retry and the direct exact-version Gradle build both confirm the Forge project builds cleanly.

## Problems intentionally not touched

- Historical release tags/assets and their mismatched provenance; they are documented but intentionally unchanged.
- Runtime networking, rendering, HTTP endpoints, configuration keys, file formats, and user data.
- The open Nether rendering issue (#1).
- CI/test-suite creation; outside this phase's build-blocker scope.

## Remaining risks and regressions

- A first Forge wrapper download attempt saw a transient Java HTTP EOF, although the same wrapper subsequently downloaded Gradle 8.1.1 and built successfully. A clean CI runner should still exercise this first-download path.
- There remains no automated test suite or CI workflow, so runtime behavior is not covered by this phase.
- Historical releases cannot be proven reproducible from their public tag references; follow the new release procedure for future releases.

## Fabric/Forge alignment for this phase

**Aligned for Phase 01 scope.** Both loader lines now track a standard wrapper JAR, expose CC0-1.0 license metadata backed by the Fabric root license, build version 0.2.1 successfully with their configured Gradle version, and include loader-appropriate narrow compatibility metadata. Runtime behavior was not altered or revalidated beyond build packaging.
