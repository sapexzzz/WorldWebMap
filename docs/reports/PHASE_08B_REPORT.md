# Phase 08B — disposable live-server validation

An official Forge 47.2.0 server was installed only under `/tmp/world-web-map-runtime.0ROAUz/forge-server`, with copied previously accepted EULA and fresh JAR `c17ae672389ffb4b3c6262f0948ef62b200df87efa78ff4a1b24114a4cdadb03`. Initial launch failed before mod initialization because `[47.2.0,47.2.0]` is an invalid Maven range. It is corrected to `[47.2.0,47.2.1)`; a fresh build and live rerun are still required.

The matching Fabric disposable validation confirmed forced generation and added the mirrored no-generation command/auto-render fix and regression test. No production world was used.

RELEASE STATUS: BLOCKED — Forge live validation and remaining dimension/performance checks are required.
