# Contributing

Choose the active independent loader branch (`2.2fabric` or `2.2forge`) and use Java 17. The `2.1` branches are archived; do not assume loader histories are shared.

Before a PR or release-validation request:

```bash
./gradlew --no-daemon --console=plain clean check build
git diff --check
```

Use the committed wrapper, keep local audit reports under ignored `docs/reports/`, and do not move historical tags. Use disposable or copied worlds for runtime/chunk-loading tests.
