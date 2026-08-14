# Phase 08B — disposable live-server validation

Fabric used only `/tmp/world-web-map-runtime.0ROAUz/fabric-server`, a copied previously accepted EULA, Fabric 0.19.2, Fabric API 0.92.9, and fresh JAR `428243d3161e74f44fa8fd5ca4e63a4192d80609894617af6f988a9a3a60c84f`. Startup, loopback HTTP, status/config/players/biome API, marker privacy, disable/enable, and port reload passed. Initial live validation exposed and fixed biome-handler server installation ordering.

Tiny fullrender/stoprender passed functionally and created valid 256x256 PNGs without `.tmp` files, but emitted 9.9s/3.6s `Can't keep up` warnings: performance is a warning, not a benchmark pass. Nether/End smoke and repeated executor-cycle smoke remain unverified.

Forced render of unloaded tile `(20,20)` created new `r.9.9` through `r.10.10` files and stalled 12.2s: generation was confirmed. Commands and auto-render were changed to never request unloaded chunks; regression test added. Rebuilt Fabric test of `(30,30)` created no `r.15.15` region and no stall.

Forge official 47.2.0 installation completed, but initial launch failed before mod initialization because `[47.2.0,47.2.0]` is an invalid Maven range. The range was changed to `[47.2.0,47.2.1)`; Forge live rerun remains required.

RELEASE STATUS: BLOCKED — Forge runtime rerun and remaining live performance/dimension checks are required.
