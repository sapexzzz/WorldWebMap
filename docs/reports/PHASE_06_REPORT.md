# Phase 06 — regression gate, CI, dependency hygiene, and release validation

Forge `/home/mentality/Scripts/java-world-web-map-forge-phase02b` remains on `2.1forge` at `bc1cd58de09661bcc197df9fb207a6002586b6e1`, with no reset/recreation or release/tag changes. Its loader-specific CI targets only `2.1forge`, uses Java 17, the committed Gradle 8.1.1 wrapper, `clean check build`, least-privilege `contents: read`, artifact validation/SHA-256, and 14-day non-release artifacts. Manual release validation is non-publishing and records exact SHA/version/artifact provenance.

Weekly Gradle/GitHub Actions Dependabot visibility is configured without auto-merge. Dependency verification metadata remains absent by documented deliberate decision. Local YAML parsing, wrapper checks, clean build (99 tests), Forge artifact validation, and temporary negative metadata validation all pass. Metadata is version `0.2.1`, license `CC0-1.0`, Forge `[47.2.0,47.2.0]`, Minecraft `[1.20.1,1.20.1]`. Remote Actions remains unverified until push; historic releases/tags were untouched. See the Fabric report for full common policy and branch-protection recommendations.

FINAL POST-REPORT `git diff --check`: Forge PASS.

PHASE 06 COMPLETE — READY FOR PHASE 07
