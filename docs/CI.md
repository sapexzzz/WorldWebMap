# CI and future release policy

Each active loader branch owns its workflow: `2.2fabric` runs Fabric CI and `2.2forge` runs Forge CI. The archived `2.1` branches are retained only as frozen history. Normal CI has `contents: read` only, validates the committed wrapper under Java 17, runs `clean check build`, validates the primary JAR, and logs SHA-256. It never publishes.

Release validation is manual and non-publishing. A future release asset must be built in CI from the exact commit/tag SHA associated with that release; source tag and binary artifact provenance must be identical and recorded.

Recommended protection for both release branches: require PR review and the loader CI check, prohibit force pushes and deletion, and require an up-to-date branch before merge. Teams retaining direct pushes may apply those protections at release time.
