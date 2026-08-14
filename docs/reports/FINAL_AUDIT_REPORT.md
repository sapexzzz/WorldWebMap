# Final audit — release-readiness verification

## 1. Executive summary

The cumulative source/build/test gate and final disposable live-server validation are healthy in both loader branches. Phase 08E used fresh non-production worlds, final built JARs, loopback-only web services, and controllable server consoles; it passed lifecycle/API, no-generation, fullrender, stoprender, performance, Nether/End, executor-cycle, and tile-integrity checks. Remote CI is still unverified because workflows remain unpushed, and both remote branches are unprotected.

## 2–4. Worktrees, build matrix, JARs

Fabric: `2.1fabric`, `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge: `2.1forge`, `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Both retained the cumulative dirty state without reset/recreation.

| Loader | Clean `check build` | Tests | Skipped | JAR / SHA-256 |
| --- | --- | ---: | ---: | --- |
| Fabric | PASS | 104 | 0 | runtime-tested `fabricwebmap-0.2.1.jar` / `d3cd512cc387406aa2351920cdaf07d1acaba0ab9bfec8be9867dade7b123a49` |
| Forge | PASS | 103 | 0 | runtime-tested `forgewebmap-0.2.1.jar` / `d148c8c2e8882102a61f1fbb5904d7cdc58ab57dd24a02066ca0f284bbc7929c` |

Fabric JAR metadata/resources/entrypoint: version `0.2.1`, CC0-1.0, Fabric Loader `>=0.15.11`, Minecraft `~1.20.1`, `fabric.mod.json`, web assets, `FabricWebMapMod.class` — PASS. Forge: version `0.2.1`, CC0-1.0, loader `[47,)`, Forge `[47.2.0,47.2.1)`, Minecraft `[1.20.1,1.20.2)`, `META-INF/mods.toml`, web assets, `ForgeWebMapMod.class` — PASS. A later no-source-change Forge rebuild produced `96b01e290d298d0507ccaf3ec5544daa0ca1b9461c02da13292c69b76076feb2`; runtime evidence remains tied only to the runtime-tested SHA above.

## 5. Original issue re-audit

| Area | Classification | Evidence / current truth |
| --- | --- | --- |
| Historical release tag/source mismatch | STILL OPEN (historical, documented) | Tags/assets untouched; exact-SHA future process is documented. |
| Wrapper, license, Forge root LICENSE, loader metadata, stale wrapper | CONFIRMED FIXED | Wrapper/JAR/properties, CC0 metadata/LICENSE, constraints, and removed `gradlew.orig` are present. |
| Enabled/bind/port lifecycle, chunk accumulation, marker privacy, HTTP executor/player/biome thread safety, status | CONFIRMED FIXED — UNIT + LIVE PROVEN | Phase 08E final-JAR APIs, privacy, lifecycle sanity, and ten-cycle reload smoke passed on both loaders. |
| Forced chunk generation | CONFIRMED FIXED — LIVE PROVEN | Phase 08B historically exposed unsafe generation; Phase 08E source audit found no production `loadChunks=true` path and fresh target `(30,30)` created no target region on either loader. |
| Duplicate tiles, temp collision, force/capacity, executor backlog, area overflow, priority, stoprender, snapshot pressure | CONFIRMED FIXED — UNIT + LIVE PROVEN | Bounded queue/admission/plans regressions pass; Phase 08E stoprender, executor, and PNG integrity smoke passed. |
| Synchronous million-job fullrender / spawn origin | CONFIRMED FIXED — UNIT + LIVE PROVEN | O(1) cursor/32-job producer contracts and final radius-1 status lifetime smoke passed. |
| Nether roof, tile-size contract, actual world path, inside flag, dimension fallback/collision, biome Y, corrupt overwrite, metric | CONFIRMED FIXED — UNIT + LIVE PROVEN | Regression coverage remains green; final Nether and End PNG smoke passed on both loaders. |
| Biome request flood/cache dimension | CONFIRMED FIXED | Debounce/cache/serial contracts pass. |
| Process-memory checksum | OPTIONAL IMPROVEMENT | First unchanged post-restart render can rewrite PNG; no correctness loss. |
| External Leaflet CDN | STILL OPEN (documented deployment limitation) | `unpkg` remains used without local bundle/SRI; UI needs client CDN access. |
| Monorepo README/CHANGELOG claims, no tests/CI/dependency visibility/provenance workflow | CONFIRMED FIXED locally | Branch-local docs, 207 total tests, workflows/Dependabot/validators/provenance process exist locally. |
| Branch protection | STILL OPEN (release process) | GitHub API reports both branches unprotected. |
| Forge artifact SHA reproducibility | OPTIONAL / RELEASE-PROCESS HARDENING | A no-source-change post-runtime rebuild changed bytes; live evidence remains attached to its recorded runtime-tested SHA. |

## 6–8. Live, forced-chunk, and performance smoke

**Historical Phase 08 finding, superseded by Phase 08E.** Fresh disposable Fabric and official Forge 47.2.0 servers now supplied the full controllable smoke matrix. Final-artifact forced-target experiments observed no target-region generation, fullrender/stoprender/performance checks passed, and Nether/End/tile/executor evidence is recorded in `FINAL_AUDIT_RUNTIME_ADDENDUM_08E.md`.

## 9–10. CI and branch protection

Local YAML, wrapper, build, artifact, negative metadata, and docs validators pass. Workflows correctly target only their loader branches, use Java 17/wrapper/`clean check build`/`contents: read`, validate JARs, calculate SHA-256, and provide manual non-publishing release validation. GitHub read-only inspection found no remote workflows/runs; status is **REMOTE ACTIONS UNVERIFIED**. Both `2.1fabric` and `2.1forge` are unprotected with no required checks. Do not change protection in this audit; require remote CI and protection before a real release.

## 11. Security final pass

No obvious tracked-source secret found in scoped scan. The only secret-pattern match was a harmless test fixture. Directory validation/storage sanitization, bounded queues/executors, command permission level 2, disabled-player `[]`, and controlled handler error responses remain present. CORS is permissive by design for the web UI; deployers must restrict bind/network exposure as documented.

## 12. Documentation/provenance final pass

README/config/contributor/CI/provenance docs match current branch topology, commands, keys, API/status fields, storage, dimensions, Nether/fullrender/stop semantics, final no-generation behavior, checksum caveat, and CDN Leaflet limitation. Historical provenance warning remains explicit; old tags/releases were not changed.

## 13. CONFIRMED FIXED

Wrapper/legal/metadata parity; lifecycle/privacy/thread-safety fixes live-proven; forced-generation prevention live-proven; queue/backpressure/dedup/temp safety; bounded incremental fullrender/status/stop semantics live-proven; Nether/End/storage/corrupt-tile correctness live-proven; final small generated-area performance smoke passed; local CI/release-validation/dependabot/docs and 207-test regression coverage.

## 14. STILL OPEN / BLOCKED / UNVERIFIED

- **CONFIRMED FIXED:** Phase 08E final-artifact live-server smoke, lifecycle/render/API validation, forced-chunk no-generation classification, Nether/End, executor-cycle, tile integrity, and small generated-area performance smoke; see `FINAL_AUDIT_RUNTIME_ADDENDUM_08E.md`.
- **PROCESS PENDING:** remote workflows are not pushed/run and both release branches are unprotected; these are not local runtime blockers.
- **Historical:** old release provenance remains mismatched and intentionally immutable.
- **Open deployment limitation:** Leaflet depends on unpkg CDN without local fallback/SRI.

## 15. OPTIONAL IMPROVEMENTS

Persistent checksum comparison; Gradle dependency verification metadata; local Leaflet bundling/SRI; richer live telemetry; eventual architecture deduplication across loader histories.

## 16–17. Release-readiness verdict

- **Fabric: READY FOR REMOTE CI / RELEASE PROCESS WITH DOCUMENTED LIMITATIONS.** Final local lifecycle/render/no-generation/performance/Nether/End/executor evidence is recorded in Phase 08E.
- **Forge: READY FOR REMOTE CI / RELEASE PROCESS WITH DOCUMENTED LIMITATIONS.** Same, with a documented post-build SHA reproducibility difference.

## 18. Post-fix scores

| Dimension | Score | Reason |
| --- | ---: | --- |
| Correctness | 9 | 207 deterministic tests plus final-JAR lifecycle, no-generation, rendering, and integrity smoke passed. |
| Security | 7 | Privacy/path/bounds improved; CDN/CORS and no dependency verification remain. |
| Architecture | 8 | Bounded seams and lifecycle ownership are clear; independent loader histories still duplicate implementation. |
| Performance | 8 | Final small generated-area smoke passed without repeated stalls; this is not a production-scale benchmark. |
| Testability | 9 | 207 passing tests, focused validators, and controllable disposable-server evidence. |
| Documentation | 9 | Branch-local truthful onboarding/config/provenance. |
| Developer experience | 8 | Wrapper/CI/docs/release validation are clear locally. |
| Release readiness | 8 | Local release gate passes; remote CI/protection and documented deployment/reproducibility limitations remain. |

## 19. Exact next-release checklist

1. Intentionally review cumulative diff in Fabric.
2. Intentionally review cumulative diff in Forge.
3. Commit Fabric changes.
4. Commit Forge changes.
5. Push both loader branches.
6. Verify remote CI passes.
7. Configure/review branch protection and required checks.
8. Decide next patch version.
9. Update all version metadata consistently.
10. Build/validate the exact release commit in remote/manual release-validation.
11. Record exact artifact SHA-256.
12. Create NEW loader-specific tags/releases from exact validated commits.
13. Never move/reuse historical broken tags.
14. Preserve release provenance record.

## 20–21. Phase 08 files and final checks

Phase 08 introduced runtime evidence and final-report reconciliation in addition to the earlier implementation/docs work. FINAL POST-REPORT `git diff --check`: Fabric PASS; Forge PASS.

FINAL AUDIT COMPLETE — LOCAL RELEASE GATE PASSED — READY FOR REMOTE CI/RELEASE PROCESS
