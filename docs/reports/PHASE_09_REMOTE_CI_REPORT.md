# Phase 09 — remote CI gate

## 1. Initial canonical SHAs

Fabric began at `d171c30ee664164745e6b6ed65be0e7a29018f15`; Forge began at `bc1cd58de09661bcc197df9fb207a6002586b6e1`.

## 2. Cumulative diff review

Reviewed complete tracked/untracked scope in both worktrees before staging. Accepted scope was remediation source, tests, wrapper, metadata, CI/workflows, validators, docs, and reports. Forge additionally deleted obsolete `gradlew.orig`.

## 3. Sensitive/runtime artifact preflight

No obvious sensitive/runtime artifact found in staged scope. Disposable server/world data was outside the repositories. Local `logs/` directories and build outputs remained unstaged.

## 4. Pre-commit Fabric validation

PASS: Java 17 `clean check build` (104 tests), artifact validator, documentation validator, YAML parsing, and `git diff --check`.

## 5. Pre-commit Forge validation

PASS: Java 17 `clean check build` (103 tests), artifact validator, documentation validator, YAML parsing, and `git diff --check`.

## 6. Fabric staged-file summary

One intentional cumulative remediation scope: 91 files, source/runtime fixes, 104-test suite, wrapper, CI, validators, docs, and reports; no runtime artifacts.

## 7. Forge staged-file summary

One intentional native Forge remediation scope: 87 files, including source/runtime fixes, 103-test suite, wrapper/license/metadata, CI, validators, docs, and deletion of `gradlew.orig`; no runtime artifacts.

## 8. Fabric commit SHA

Cumulative commit `793563f877a68775dc45b439b07396e2d8fb7ca4`; CI portability fix `a73fe2c3cced114cbd09a28aa95f382c8b472662`.

## 9. Forge commit SHA

Cumulative commit `102591b07e88a029cd90f7446f821e98da4d77fd`; CI portability fix `cfb3b68232ad8ddb46dd2c75df7b0645aa949162`.

## 10. Post-commit worktree status

Accepted cumulative changes were committed. Only untracked local `logs/` directories remained; they were deliberately excluded.

## 11. Fabric push result

PASS: normal non-force push updated `2.1fabric` to `a73fe2c3cced114cbd09a28aa95f382c8b472662`.

## 12. Forge push result

PASS: normal non-force push updated `2.1forge` to `cfb3b68232ad8ddb46dd2c75df7b0645aa949162`.

## 13. Remote branch SHA confirmation

GitHub API confirmed both remote branch SHAs exactly matched the local CI-fix commits.

## 14. Fabric remote CI exact run/result

Initial run `31847673220` failed only because hosted Ubuntu lacks `rg`. Corrected exact-SHA run `31847911111` on `a73fe2c3cced114cbd09a28aa95f382c8b472662` passed (`Fabric CI`, check `build`).

## 15. Forge remote CI exact run/result

Initial run `31847673926` had the same documentation-validator portability failure. Corrected exact-SHA run `31847912077` on `cfb3b68232ad8ddb46dd2c75df7b0645aa949162` passed (`Fabric CI` workflow label on this independent branch, check `build`).

## 16. Any CI-only failure/fix commits

Both first runs failed in `Validate developer documentation`: `rg: command not found`. The only fix was a shared `grep -F` fallback in `scripts/validate-docs.sh`, committed separately without rewriting either cumulative commit.

## 17. Manual release-validation result

PASS, non-publishing dispatches: Fabric run `31848133407`; Forge run `31848134998`. Both checked Java 17, version `0.2.1`, clean build, wrapper, metadata, and SHA-256. No release/tag publication step exists in the workflow or was performed.

## 18. Remote artifact/checksum evidence

Fabric ordinary CI SHA `d3cd512cc387406aa2351920cdaf07d1acaba0ab9bfec8be9867dade7b123a49`; manual validation matched it. Forge ordinary CI SHA `ca073b4867f83f1c0c1d5708ff94cb0dc7e571815f6344fd45c37ed5816d5462`; manual validation SHA `6393bfc66ee9f37eceac0cd59ded6b118033c8165c390f04b519920cc9903fdb`.

## 19. Forge reproducibility observation

Different no-source-change local/remote Forge JAR bytes were observed. This remains release-process hardening, not a CI failure. Any real release asset must be the exact recorded/validated artifact for its release commit.

## 20. Branch-protection current state

GitHub API reports both `2.1fabric` and `2.1forge` are not protected. No protection setting was changed.

## 21. Recommended required-check names

Require check run `build` from the loader CI workflow on each respective branch; block force pushes and deletion. Consider required review and up-to-date checks according to the repository workflow.

## 22. Confirmation no tags/releases were created

Confirmed: no tags or GitHub releases were created, moved, or published in Phase 09. Historical releases/tags were only read.

## 23. Exact remaining release-process steps

Commit and push this report-only documentation commit; verify its triggered ordinary CI remains green; configure/review branch protection; choose/update a next patch version; validate the exact future release commit and record its artifact SHA; then create new loader-specific tags/releases without moving historical tags.

REMOTE CI GATE PASSED — READY FOR BRANCH PROTECTION AND VERSIONED RELEASE PREPARATION
