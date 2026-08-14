# Phase 06 — regression gate, CI, dependency hygiene, and release validation

## 1–3. Canonical worktrees and topology

Fabric is `/home/mentality/Scripts/java-world-web-map`, branch `2.1fabric`, HEAD `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge is `/home/mentality/Scripts/java-world-web-map-forge-phase02b`, branch `2.1forge`, HEAD `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Both retained their accepted dirty cumulative status and were not reset/recreated. The Phase 02 cumulative report and Phase 03F, 04D, and 05B reports were read; Phase 05B ends `PHASE 05 COMPLETE — READY FOR PHASE 06`.

The histories are independent, so each branch now owns an equivalent but loader-specific workflow. Fabric CI targets only `2.1fabric`; Forge CI targets only `2.1forge`. No workflow checks out or builds the other loader.

## 4–8. CI, permissions, wrapper, and regression gate

Each `.github/workflows/ci.yml` triggers on push and pull requests targeting its loader branch plus `workflow_dispatch`. It runs on Ubuntu with Java 17 (`actions/setup-java@v4`, Temurin) and Gradle dependency cache, uses `actions/checkout@v4`, and has only `permissions: contents: read`.

The workflow explicitly checks committed wrapper JAR/properties, executable `gradlew`, the loader-specific distribution (`8.8` Fabric, `8.1.1` Forge), and `./gradlew --version`. Its source-of-truth command is `./gradlew --no-daemon --console=plain clean check build`, so all JUnit tests/checks execute. Test/JAR artifacts are retained for 14 days; normal CI has no write, package, release, or tag permission.

## 9–11. Artifact and release validation

Loader-aware `scripts/validate-artifact.sh` validates the actual primary JAR metadata. CI logs `sha256sum` and writes a checksum beside the primary JAR. Local checksums: Fabric `530f59d75f99c063c0dc636968f2fb7411c4879dfa99c9866736c33bc8c0fe00`; Forge `f73f3a89e48dce83fabf60efe95ee2940fb73acb351061ef7fb4ed8256acc4f6`.

The separate manual `release-validation.yml` prints exact commit SHA, validates wrapper, compares the requested version with `gradle.properties`, runs clean build/JAR validation/SHA-256, and prints branch/commit/version/artifact provenance. It has no publishing action.

## 12–15. Dependencies, action choices, cache, and policy

Dependabot is configured weekly for Gradle and GitHub Actions, without auto-merge. Gradle verification metadata is absent in both branches; it was not added because generating it now would create large lock/checksum policy files across independent loader dependency graphs. This is optional future hardening, not fabricated manually.

Only established maintained actions are used: checkout, setup-java, and upload-artifact, all at major v4. `setup-java` owns dependency caching; no build output is treated as source of truth. `docs/CI.md` recommends PR + CI protection, no force push/deletion, and up-to-date merging for release branches while distinguishing optional direct-push development flow.

## 16–17. Local workflow/script validation

All six YAML files (two CI, two release validation, two Dependabot) parse with local PyYAML. Referenced scripts and paths exist and run on Linux with no secrets. `scripts/test-validate-artifact.sh` passes against both built primary JARs and creates only temporary negative JARs: missing metadata, wrong version, and license mismatch all fail as expected. No built JAR was modified.

## 18–21. Full local validation and metadata

Fabric complete clean build: PASS, **100** tests. Forge complete clean build: PASS, **99** tests. Earlier Phase 02–05 regressions remain included in these full suites.

Exact metadata validated by the scripts: Fabric `fabricwebmap-0.2.1.jar`, version `0.2.1`, license `CC0-1.0`, Fabric Loader `>=0.15.11`, Minecraft `~1.20.1`; Forge `forgewebmap-0.2.1.jar`, version `0.2.1`, license `CC0-1.0`, Forge `[47.2.0,47.2.0]`, Minecraft `[1.20.1,1.20.1]`.

## 22–25. Final checks, remote status, risk, parity

FINAL POST-REPORT `git diff --check`: Fabric PASS; Forge PASS. Remote GitHub Actions execution remains unverified because no workflow was pushed, but local YAML/wrapper/build/script validation passed and ordinary CI cannot publish. Historical tags/releases were untouched. Remaining risks are remote-runner-only behavior and optional future Gradle dependency verification metadata. Fabric/Forge CI policy and validation behavior are parity-aligned.

PHASE 06 COMPLETE — READY FOR PHASE 07
