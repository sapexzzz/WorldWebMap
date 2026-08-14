# Final audit reconciliation — Phase 08F

## 1. Canonical worktrees + SHAs

Fabric is `/home/mentality/Scripts/java-world-web-map` on `2.1fabric` at `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge is `/home/mentality/Scripts/java-world-web-map-forge-phase02b` on `2.1forge` at `bc1cd58de09661bcc197df9fb207a6002586b6e1`. Cumulative dirty state was preserved.

## 2. Stale final-report items found

The active final audit retained pre-08E 100/99 test totals, old JAR hashes, invalid exact Forge/Minecraft ranges, unit-only/unverified runtime prose, a pre-runtime scorecard, and an obsolete disposable-smoke checklist step.

## 3. Corrected test totals

Fabric: 104 PASS, 0 skipped. Forge: 103 PASS, 0 skipped. Total: 207 deterministic passing tests.

## 4. Corrected Forge metadata

Version `0.2.1`, license `CC0-1.0`, loader `[47,)`, Forge `[47.2.0,47.2.1)`, Minecraft `[1.20.1,1.20.2)`.

## 5. Phase 08E runtime evidence incorporated

The final report now records final-JAR startup/API, lifecycle/privacy, fullrender/stoprender, generated-area performance smoke, Nether/End, executor-cycle, and PNG-integrity PASS results for both loaders.

## 6. Forced-generation final classification

CONFIRMED FIXED — LIVE PROVEN. Historical Phase 08B unsafe generation context remains recorded. Phase 08E found no production `loadChunks=true` caller; fresh Fabric and Forge target `(30,30)` experiments created no target region and skipped unavailable terrain.

## 7. Performance final classification

CONFIRMED IMPROVED / LIVE SMOKE PASS. Final small generated-area radius-1 repeats were responsive with no repeated stall warnings. This is not a production-scale benchmark; pre-fix severe stalls are superseded by final-JAR evidence.

## 8. Corrected issue re-audit classifications

Runtime/lifecycle/API, forced generation, queue/stoprender, fullrender, and dimension/tile correctness are now classified as live-proven where Phase 08E supplied evidence. Remote CI remains UNVERIFIED / PROCESS PENDING; branch protection remains an OPEN RELEASE-PROCESS ITEM; Leaflet CDN remains a DOCUMENTED DEPLOYMENT LIMITATION; Forge SHA reproducibility is OPTIONAL / RELEASE-PROCESS HARDENING.

## 9. Final scores

Correctness 9 (deterministic plus live runtime); Security 7 (CDN/CORS/dependency limitations remain); Architecture 8 (bounded/lifecycle seams proven, duplicated histories remain); Performance 8 (final smoke passed, no production benchmark); Testability 9 (207 tests and controllable runtime harness); Documentation 9 (reconciled source-of-truth audit); Developer experience 8 (local workflow clear); Release readiness 8 (local gate passed, remote process pending).

## 10. Final Fabric release-process verdict

READY FOR REMOTE CI / RELEASE PROCESS WITH DOCUMENTED LIMITATIONS.

## 11. Final Forge release-process verdict

READY FOR REMOTE CI / RELEASE PROCESS WITH DOCUMENTED LIMITATIONS.

## 12. Forge reproducibility limitation

Phase 08E runtime evidence corresponds only to runtime-tested SHA `d148c8c2e8882102a61f1fbb5904d7cdc58ab57dd24a02066ca0f284bbc7929c`. A no-source-change post-runtime rebuild produced `96b01e290d298d0507ccaf3ec5544daa0ca1b9461c02da13292c69b76076feb2`; this reconciliation build produced `adbd0fe68a576aa471d9b106e72166ae0f07f8c766aa00785c695996d15d074c`. These are reproducibility limitations, not mixed runtime evidence.

## 13. Remote CI/branch-protection state

Remote GitHub Actions have not run because branches were not pushed in this local task. Both branches remain unprotected. These are release-process requirements, not local runtime blockers.

## 14. Corrected next-release checklist

1. Review Fabric cumulative diff.
2. Review Forge cumulative diff.
3. Commit Fabric changes.
4. Commit Forge changes.
5. Push both branches.
6. Verify remote CI.
7. Configure/review protection and required checks.
8. Decide next patch version.
9. Update version metadata.
10. Build/validate the exact release commit.
11. Record exact SHA-256.
12. Create new loader-specific tags/releases from exact commits.
13. Never move/reuse broken historical tags.
14. Preserve provenance.

## 15. Files changed

`docs/reports/FINAL_AUDIT_REPORT.md` and this reconciliation report only.

## 16. Final deterministic Fabric build/test result

PASS: Java 17 `clean check build`, 104 tests; SHA `d3cd512cc387406aa2351920cdaf07d1acaba0ab9bfec8be9867dade7b123a49`.

## 17. Final deterministic Forge build/test result

PASS: Java 17 `clean check build`, 103 tests; reconciliation-build SHA `adbd0fe68a576aa471d9b106e72166ae0f07f8c766aa00785c695996d15d074c` (not runtime evidence).

## 18. Artifact/docs/YAML validation

PASS for both loaders: artifact validators, docs validators, and workflow/Dependabot YAML parsing. Forge metadata validation confirmed the final accepted ranges.

## 19. Final post-report `git diff --check` both

PASS, run after this report.

## 20. Exact remaining limitations

Remote Actions unverified; branches unprotected; Leaflet uses unpkg CDN; Forge byte-for-byte JAR reproducibility varies across no-source-change builds; historical release provenance mismatch remains documented and untouched. None is a local runtime blocker.

FINAL AUDIT RECONCILED — READY FOR COMMIT/PUSH AND REMOTE CI
