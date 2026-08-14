# Phase 07 — documentation, repository truthfulness, and developer experience

## 1–3. Baseline and topology

Fabric `/home/mentality/Scripts/java-world-web-map` remains `2.1fabric` at `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` remains `2.1forge` at `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Both retained their cumulative dirty states without reset/recreation. Phase 06 was confirmed complete.

READMEs no longer describe nonexistent sibling directories. Each states its independent branch, exact wrapper commands, Java/toolchain/version, and primary JAR path. They explicitly say shared fixes must be mirrored and validated across independent histories.

## 4–10. Runtime documentation audit

`README.md` now documents branch-local build/test/deploy, permission-level-2 commands, spawn-centred lazy fullrender, cancellation semantics, API/privacy behavior, reversible dimension names, Nether surface behavior, worker/snapshot model, real storage roots, checksum limitation, and forced-chunk-load uncertainty.

`docs/CONFIGURATION.md` documents every current `WebMapConfig` key with type/default, range/allowed values, effect, fallback behavior, and reload/restart guidance. It explicitly records fixed 256 tile size, 10ms default snapshot budget, privacy behavior, and safe bind/port service replacement.

API documentation reflects the current `/`, tile, status, config, players, and biome endpoints. `renderedTiles` means WRITTEN results only; disabled markers return `[]`; biome requests reject unknown dimensions and may return transient 503. `/api/status` fullrender fields are documented. No unsupported speed/TPS claim is made.

## 11–14. CI, provenance, contributing, and cleanup

README links the Phase 06 CI/manual release validation path and preserved provenance record. `docs/RELEASE_PROVENANCE.md` retains historic mismatch facts and exact-SHA release rules. New `CONTRIBUTING.md` gives branch selection, Java 17, wrapper build, diff check, report/tag, and disposable-world guidance. Changelog adds an unreleased maintenance section without claiming a release.

The focused stale-path scan found no obsolete monorepo paths in active README/contributor/config documentation; historical reports retain historical facts intentionally. No runtime behavior or release metadata was changed.

## 15–16. Fresh-reader and docs validation

A fresh reader can select the loader branch, verify Java/wrapper, run `clean check build`, locate the JAR/config/CI/provenance docs, and follow contributor validation without hidden directory knowledge. New `scripts/validate-docs.sh` checks branch command, JAR path, snapshot config, status/dimension documentation, and rejects stale local monorepo links. It runs in each loader CI workflow and passed locally.

## 17–23. Files, validation, metadata, and remote status

Changed files are loader READMEs, configuration references, CONTRIBUTING guides, changelogs, documentation validators, CI workflow steps, and this report. Fabric clean build: PASS, **100** tests. Forge clean build: PASS, **99** tests.

Metadata remains Fabric version `0.2.1`, license `CC0-1.0`, Fabric Loader `>=0.15.11`, Minecraft `~1.20.1`; Forge version `0.2.1`, license `CC0-1.0`, Forge `[47.2.0,47.2.0]`, Minecraft `[1.20.1,1.20.1]`.

FINAL POST-REPORT `git diff --check`: Fabric PASS; Forge PASS. Remote GitHub Actions status is unchanged: workflows are locally validated but unverified on GitHub runners until pushed.

## 24–25. Remaining risk and parity

Remaining risks are live-server forced-chunk generation behavior, tick/worker throughput, and remote CI execution. Fabric and Forge documentation has equivalent scope with loader-specific values and paths.

PHASE 07 COMPLETE — READY FOR PHASE 08
