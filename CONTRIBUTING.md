# Contributing

Choose the correct independent loader branch (`2.1fabric` or `2.1forge`) and use Java 17. Do not assume the branches share history; mirror shared fixes deliberately and validate each loader separately.

Before a PR or release-validation request:

```bash
./gradlew --no-daemon --console=plain clean check build
git diff --check
```

Use the committed wrapper, keep reports under `docs/reports/`, and do not move historical tags. Use disposable or copied worlds for runtime/chunk-loading tests.
