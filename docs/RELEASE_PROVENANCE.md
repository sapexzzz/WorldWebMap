# Release provenance

Historical 2.1 release references are not a reproducible source-of-truth:

- `refs/tags/2.1fabric` (`18c895687fb35bb3be8d4a2f55d296e0cd607ac8`) contains Forge 0.2.0 source, not Fabric 0.2.1 source.
- `refs/tags/2.1forge` (`d4d610f8caf281daa75d254e96b50bd6de49c9e3`) also contains Forge 0.2.0 source, not the Forge 0.2.1 source branch.
- The published `2.1fabric` assets are named Fabric 0.2.1, and the published `2.1forge` assets are named Forge 0.2.1. GitHub's release API additionally records `target_commitish` as `2.1forge` and `2.0forge`, respectively.

Those historical tags and releases are public records and must not be moved or replaced.

## Safe procedure for the next patch release

1. Make the loader-specific change on its own loader branch; do not merge the independent histories.
2. Set the loader's `mod_version` to the intended patch version and build from a clean checkout using the committed wrapper and Java 17.
3. Inspect the produced JAR: confirm its loader metadata, mod ID, version, and license match the branch.
4. Commit the exact source and build metadata, then create one new, unambiguous loader-specific annotated tag at that commit (for example, `v0.2.2-fabric` or `v0.2.2-forge`). Never reuse or move historical tags.
5. Verify the tag resolves to the intended commit SHA, build again from that exact tag in a clean checkout, and compare the inspected metadata before uploading only those resulting artifacts to a new release.
6. Record the tag SHA and artifact checksums in the release notes.

## CI release validation

Use the loader-specific manual release-validation workflow before publishing. It validates the committed wrapper, exact checked-out SHA, clean build, JAR metadata, and SHA-256 without creating a release. A release asset must be built in CI from the exact commit/tag SHA associated with that release; source tag and binary artifact provenance must be identical and recorded.
