# Phase 00 — Governance, baseline inventory, and safe working state

Audit date: 2026-08-12 (Europe/Paris). This phase made no runtime, build, configuration, endpoint, or user-data change. The only working-tree change is this audit report.

## Scope and Git state

- Repository: `sqwiziiy/World-Web-Map`; public; default branch `2.1fabric`.
- Local working branch at audit start: `2.1fabric`.
- Initial `git status --short`: clean (no output).
- Relevant branch refs:
  - `refs/heads/2.1fabric` = `d171c30ee664164745e6b6ed65be0e7a29018f15`
  - `refs/remotes/origin/2.1fabric` = `d171c30ee664164745e6b6ed65be0e7a29018f15`
  - `refs/remotes/origin/2.1forge` = `bc1cd58de09661bcc197df9fb207a6002586b6e1`
- Relevant tags (all lightweight commit tags):
  - `2.1fabric` = `18c895687fb35bb3be8d4a2f55d296e0cd607ac8`
  - `2.1forge` = `d4d610f8caf281daa75d254e96b50bd6de49c9e3`
- `git merge-base refs/remotes/origin/2.1fabric refs/remotes/origin/2.1forge` produced no commit, and neither ref is an ancestor of the other. **CONFIRMED:** the target loader branches are independent histories.
- Other remotes present at inspection: `origin/1.0forge`, `origin/2.0fabric`, and `origin/2.0forge`. No additional local branches were present.

The short name `2.1fabric` is ambiguous locally because both a branch and a tag exist. All audit operations therefore use fully qualified refs.

## Complete target-tree inventory

Both trees contain `.gitignore`, `CHANGELOG.md`, `README.md`, `build.gradle`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`, `gradlew`, `gradlew.bat`, and `settings.gradle`.

Fabric additionally contains `LICENSE`, `src/main/resources/fabric.mod.json`, `src/main/resources/pack.mcmeta`, and the following Java paths under `src/main/java/com/mentality/fabricwebmap/`:

- `FabricWebMapMod.java`
- `commands/WebMapCommands.java`
- `config/WebMapConfig.java`
- `player/PlayerMarkerService.java`
- `render/BlockColorResolver.java`, `ChunkLoadListener.java`, `RenderJob.java`, `TileRenderManager.java`, `TileRenderer.java`, `TileSnapshot.java`, `TileSnapshotBuilder.java`
- `storage/TileStorage.java`
- `util/DimensionUtil.java`, `JsonUtil.java`
- `web/ApiBiomeHandler.java`, `ApiConfigHandler.java`, `ApiPlayersHandler.java`, `ApiStatusHandler.java`, `StaticFileHandler.java`, `TileHttpHandler.java`, `WebServerService.java`

Forge additionally contains `gradlew.orig`, `src/main/resources/META-INF/mods.toml`, `src/main/resources/pack.mcmeta`, and the same Java path layout under `src/main/java/com/mentality/forgewebmap/`, with `ForgeWebMapMod.java` in place of `FabricWebMapMod.java`. Both trees include the identical frontend files `src/main/resources/web/app.js`, `index.html`, and `style.css`.

No tracked `.github/` path, test source/path, or test-named file exists in either target tree.

## Build and runtime metadata confirmed from tracked files

| Item | Fabric (`d171c30`) | Forge (`bc1cd58`) |
| --- | --- | --- |
| Minecraft | 1.20.1 | 1.20.1 |
| Java toolchain / compiler release | 17 / explicit 17 | 17 / no explicit `options.release` |
| Build system | Gradle, Fabric Loom `1.7.4` | Gradle, ForgeGradle `6.0.16` |
| Loader/API | Fabric Loader `0.15.11`; Fabric API `0.92.2+1.20.1` | Forge `47.2.0`; mods.toml permits `[47,)` |
| Wrapper distribution | Gradle 8.8 | Gradle 8.1.1 |
| Mod version | 0.2.1 | 0.2.1 |
| HTTP server | `com.sun.net.httpserver.HttpServer` in `web/WebServerService.java` | same |
| Web client | tracked vanilla JavaScript/CSS/HTML under `src/main/resources/web/` | byte-identical to Fabric |

The local JDK is OpenJDK 26.0.2. This does not satisfy the project baseline Java 17 for a build; no JDK was installed or changed.

## Expected-finding verification

| Expected finding | Result | Evidence |
| --- | --- | --- |
| `gradle-wrapper.jar` appears missing | **CONFIRMED** | `gradle/wrapper/gradle-wrapper.jar` is absent in both complete Git trees; both `./gradlew --version` invocations fail with `ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain`. |
| GitHub Actions appear absent | **CONFIRMED** | Neither target tree has a `.github/` entry. |
| Tests appear absent | **CONFIRMED** | Neither target tree contains a test path or test-named source file. |
| Loader lines may be independent histories | **CONFIRMED** | No merge base; neither branch is an ancestor of the other. |
| Release tags may not point at source used for published loader binaries | **CONFIRMED** | Both `refs/tags/2.1fabric` and `refs/tags/2.1forge` resolve to Forge-source trees with `forgewebmap` version 0.2.0. Published 2.1 assets are Fabric/Forge-specific 0.2.1 JARs. The exact build inputs for those artifacts cannot be reconstructed without a working wrapper or external provenance. |
| Fabric has root CC0 license while metadata claims MIT | **CONFIRMED** | Root `LICENSE` begins “Creative Commons Legal Code / CC0 1.0 Universal”; `fabric.mod.json` declares `"license": "MIT"`. |
| Forge may lack root LICENSE | **CONFIRMED** | `LICENSE` is absent from the Forge target tree. |

## Hygiene and repository controls

- No candidate credential, token, password assignment, or private-key file matched the scoped tracked-content secret scan in either target. No secret value is recorded here.
- No committed binary artifact was found in either target tree. The largest tracked files are source/docs; all tracked entries are text/script resources. The `.gitignore` deliberately excludes common archives/JARs while explicitly allowing the wrapper JAR, so its absence is not caused by that rule.
- **CONFIRMED:** Forge includes `gradlew.orig`, a second, differing executable wrapper script (four changed lines relative to `gradlew`); it is a backup-style duplicate. Fabric has no duplicate wrapper script.
- **CONFIRMED:** both trees are missing the wrapper JAR component while retaining the wrapper properties and platform scripts.
- **CONFIRMED:** no CI configuration and no automated tests are tracked.
- Open GitHub work (network available): one open issue, [#1 — display in nether.](https://github.com/sqwiziiy/World-Web-Map/issues/1); no open pull requests. It was inspected only and not changed.

## Published release inventory (read-only)

| Release | Tag | API `target_commitish` | Assets |
| --- | --- | --- | --- |
| Version 2.1fabric (latest) | `2.1fabric` | `2.1forge` | `fabricwebmap-0.2.1.jar` (64,451 bytes), `fabricwebmap-0.2.1-sources.jar` (35,670 bytes) |
| Version 2.1forge | `2.1forge` | `2.0forge` | `forgewebmap-0.2.1.jar` (63,762 bytes), `forgewebmap-0.2.1-sources.jar` (37,551 bytes) |
| Version 2.0fabric | `2.0fabric` | `2.0forge` | Fabric 0.2.0 binary and sources |
| Version 2.0forge | `2.0forge` | `2.0forge` | Forge 0.2.0 binary and sources |
| Version 1.0forge | `1.0` | `1.0` | Forge 0.1.0 binary and sources |

Release/tag names and assets were only listed. No tag, release, asset, branch, or remote state was modified.

## Validation commands and results

| Command (exact form; multi-line invocations are shown compactly) | Result |
| --- | --- |
| `git status --short` | **PASS** — clean before this report was created. |
| `git branch --show-current` | **PASS** — `2.1fabric`. |
| `git remote -v`; `git branch -a --no-color`; `git tag --list --format='%(refname:short) %(objecttype) %(objectname) %(contents:subject)'`; `git show-ref --heads --tags` | **PASS** — refs enumerated. |
| `git rev-parse refs/heads/2.1fabric refs/remotes/origin/2.1fabric refs/remotes/origin/2.1forge refs/tags/2.1fabric^{commit} refs/tags/2.1forge^{commit}` | **PASS** — exact SHAs recorded above. |
| `git merge-base refs/remotes/origin/2.1fabric refs/remotes/origin/2.1forge`; `git merge-base --is-ancestor …` both directions | **PASS** — correctly established independent histories (no merge base / exit 1 each direction). |
| `git ls-tree -r --name-only refs/remotes/origin/2.1fabric` and the Forge equivalent | **PASS** — complete trees inspected. |
| `git show <target>:{build.gradle,gradle.properties,settings.gradle,gradle/wrapper/gradle-wrapper.properties,loader metadata,pack.mcmeta}` | **PASS** — build/runtime metadata recorded. |
| `git ls-tree -r -l <target>`; tests/CI/wrapper path searches; `git cat-file -e <target>:gradle/wrapper/gradle-wrapper.jar` | **PASS** — hygiene inventory completed; JAR absence confirmed (the `cat-file` check expectedly exits 128). |
| `git grep -IlE '(BEGIN … PRIVATE KEY|AIza…|ghp_…|github_pat_…|AKIA…|xox…|password=…|secret=…|token=…) ' <target> -- .` | **PASS** — no candidate tracked secret file matched. |
| `git grep -In 'HttpServer' <target> -- '*.java'` | **PASS** — built-in server confirmed in both loaders. |
| `java -version` | **PASS** — OpenJDK 26.0.2 detected. |
| `./gradlew --version` on Fabric | **FAIL (confirmed defect)** — missing `org.gradle.wrapper.GradleWrapperMain`. |
| `git archive refs/remotes/origin/2.1forge | tar -x -C <mktemp>` then `(cd <mktemp> && ./gradlew --version)` | **FAIL (confirmed defect)** — same missing wrapper main class. |
| `gh auth status`; `gh issue list --repo sqwiziiy/World-Web-Map --state open --limit 100 --json number,title,url,updatedAt`; `gh pr list --repo sqwiziiy/World-Web-Map --state open --limit 100 --json number,title,url,headRefName,baseRefName,updatedAt`; `gh release list --repo sqwiziiy/World-Web-Map --limit 100`; `gh api repos/sqwiziiy/World-Web-Map/releases --paginate --jq '…'` | **PASS** — GitHub state/release assets inspected read-only. |

No normal Gradle build or tests could be run: both wrappers fail before Gradle starts, and neither tree has a test suite. No newer dependency or replacement JDK was installed to bypass that condition.

For audit reproducibility, the commands above were executed from `/home/mentality/Scripts/java-world-web-map`; the following are the literal target-specific forms for the compacted `<target>`/ellipsis rows:

```sh
git merge-base --is-ancestor refs/remotes/origin/2.1fabric refs/remotes/origin/2.1forge
git merge-base --is-ancestor refs/remotes/origin/2.1forge refs/remotes/origin/2.1fabric
git ls-tree -r --name-only refs/remotes/origin/2.1fabric
git ls-tree -r --name-only refs/remotes/origin/2.1forge
git ls-tree -r -l refs/remotes/origin/2.1fabric
git ls-tree -r -l refs/remotes/origin/2.1forge
git cat-file -e refs/remotes/origin/2.1fabric:gradle/wrapper/gradle-wrapper.jar
git cat-file -e refs/remotes/origin/2.1forge:gradle/wrapper/gradle-wrapper.jar
git grep -In 'HttpServer\\|HttpServer' refs/remotes/origin/2.1fabric -- '*.java'
git grep -In 'HttpServer\\|HttpServer' refs/remotes/origin/2.1forge -- '*.java'
git diff --stat refs/tags/2.1fabric^{commit} refs/remotes/origin/2.1fabric
git diff --stat refs/tags/2.1forge^{commit} refs/remotes/origin/2.1forge
git ls-tree -r --name-only refs/tags/2.1fabric^{commit}
git ls-tree -r --name-only refs/tags/2.1forge^{commit}
git show refs/tags/2.1fabric^{commit}:gradle.properties
git show refs/tags/2.1forge^{commit}:gradle.properties
gh api repos/sqwiziiy/World-Web-Map/releases --paginate --jq '.[] | {name,tag_name,target_commitish,draft,prerelease,published_at,assets:[.assets[] | {name,size,download_count,browser_download_url}]}'
```

## Files changed

- `PHASE_00_REPORT.md` — added audit documentation only.

## Confirmed problems fixed

None. Phase 00 explicitly prohibits functional fixes.

## Problems intentionally not touched

- Missing Gradle wrapper JARs (both loaders).
- Absence of CI and automated tests.
- Release tag/source provenance mismatch and published-release metadata inconsistency.
- Fabric license mismatch; missing Forge root license.
- Forge `gradlew.orig` duplicate/backup script.
- Open Nether rendering issue #1.

## Remaining risks and alignment assessment

The two loader lines are behaviorally aligned only for the narrowly inspected shared web frontend and use of the built-in HTTP server. Full behavioral alignment is **unconfirmed**: source implementations differ and neither loader can currently build via its committed wrapper. The absence of tests/CI and unreliable release-to-source provenance make regressions difficult to detect. Verify future fixes independently on both branch histories and regenerate/commit valid wrapper components before attempting normal Gradle validation.
