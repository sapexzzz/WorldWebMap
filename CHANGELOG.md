# World Web Map - Changelog

## [0.2.2] - Unreleased

- Added wrapper-based CI, artifact validation, and non-publishing release validation.
- Hardened config, queue/lifecycle/privacy, storage/dimension, and incremental fullrender controls.
- Documented loader branch topology and release provenance without creating a release.
- Opened the active 2.2 development line for both Fabric and Forge.

## [0.2.1] - 2026-05-01

### Changed
- Forge and Fabric are now both versioned as `0.2.1`.
- README, changelog, Gradle metadata, jar names, and API-reported `MOD_VERSION` are aligned across both loaders.
- Forge implementation was synchronized with the Fabric port so both loaders ship the same render behavior.

### Fixed
- Chunks disappearing during new-chunk generation: new tiles can render with `loadChunks=true` so chunks are read from disk before debounce-delayed rendering happens.
- Existing tile erased by blank re-render: renderer composites over the previously saved tile during re-renders.
- All-transparent tile written to disk: fully transparent output is skipped when there is no loaded data and no existing tile to preserve.
- Height-shading seam artifacts: neighbor height is used only when that neighbor pixel was confirmed loaded.

### Build
- Forge output: `forge/build/libs/forgewebmap-0.2.1.jar`.
- Fabric output: `fabric/build/libs/fabricwebmap-0.2.1.jar`.

---

## [0.2.0] - 2026-04-30 / 2026-05-01

### Added
- Biome API endpoint: `GET /api/biome?dim=overworld&x=100&z=200`.
- Biome display in the desktop toolbar and mobile bottom bar.
- Human-readable biome names for vanilla biomes and namespace-prefixed names for modded biomes.
- Initial Fabric port from the Forge codebase, using Fabric lifecycle, tick, chunk-load, and command registration hooks.
- Fabric config and tile paths: `fabricwebmap-common.properties` and `world/fabricwebmap/tiles/`.

### Fixed
- Biome label now updates immediately when the cursor or mobile center moves to another block.
- Fabric port included the render stability fixes that became the shared `0.2.1` behavior.

### Loader Notes
- Forge `0.2.0` introduced biome display on 2026-04-30.
- Fabric `0.2.0` was the initial Fabric port on 2026-05-01.

---

## [0.1.0] - 2026-04-29 (Forge only)

### Added
- Initial Forge implementation for Minecraft 1.20.1 / Forge 47.x.
- Built-in HTTP server based on `com.sun.net.httpserver.HttpServer`, default port `8123`.
- Static frontend serving for `index.html`, `app.js`, and `style.css`.
- Tile endpoint: `GET /tiles/{dimension}/{zoom}/{x}/{z}.png`.
- API endpoints: `/api/status`, `/api/players`, and `/api/config`.
- 2D top-down renderer with height shading and a block color palette.
- Snapshot pipeline: world data is sampled on the main server thread and PNG rendering runs in background workers.
- Tile storage for Overworld, Nether, and The End.
- Atomic tile writes using temp file plus move, with CRC32 skip for unchanged PNGs.
- Compositing for unloaded chunks so previously rendered areas can be preserved.
- Chunk-load auto-render with debounce.
- Operator commands: `/webmap status`, `/webmap render`, `/webmap render-area`, `/webmap fullrender`, `/webmap stoprender`, and `/webmap reload`.
- Leaflet web UI with coordinate display, dimension switcher, player markers, dark theme, and mobile layout.
