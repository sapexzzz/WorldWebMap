# World Web Map (Fabric) — Changelog

## [0.2.0] — 2026-05-01

_Initial Fabric port of ForgeWebMap 0.2.1. Functionality is identical to the Forge version; all bug fixes from 0.2.1 are included from the start._

### Added

#### Fabric port
- Ported from Forge 47.x to Fabric Loader 0.15.11, Minecraft 1.20.1
- Replaced Forge event bus (`@SubscribeEvent`) with Fabric event hooks:
  - `ServerLifecycleEvents.SERVER_STARTED` / `SERVER_STOPPING` — start/stop HTTP server
  - `ServerTickEvents.END_SERVER_TICK` — drive the render queue
  - `ServerChunkEvents.CHUNK_LOAD` — trigger chunk-load debounce
  - `CommandRegistrationCallback.EVENT` — register `/webmap` command tree
- Config file renamed to `fabricwebmap-common.properties` (read via `FabricLoader.getInstance().getConfigDir()`)
- Tile storage path changed to `world/fabricwebmap/tiles/`
- Mod ID: `fabricwebmap`, group: `com.mentality.fabricwebmap`

#### Biome display (from Forge 0.2.0)
- API endpoint `GET /api/biome?dim=overworld&x=100&z=200` — returns biome name and registry ID
- Biome name shown on desktop toolbar next to coordinates (🌳 Dark Forest)
- Biome name shown on mobile bottom bar below coordinates
- Biome updates instantly as the cursor moves to a different block
- Vanilla biomes formatted as human-readable names (`minecraft:dark_forest` → `Dark Forest`)
- Modded biomes prefixed with mod name (`somemod:custom_biome` → `[somemod] Custom Biome`)

#### Web Server
- Built-in HTTP server (`com.sun.net.httpserver.HttpServer`), port 8123
- Static file serving from classpath (`index.html`, `app.js`, `style.css`)
- Endpoints: `/tiles/`, `/api/status`, `/api/players`, `/api/config`, `/api/biome`

#### Map Renderer
- 2D top-down view with height shading
- Snapshot system: world data read on main thread, PNG rendered in background
- Dimension support: Overworld, Nether, The End
- Atomic tile writing via temp file + `Files.move(ATOMIC_MOVE)`
- CRC32 change check — unchanged tiles are not overwritten
- Compositing: unloaded chunks take pixels from the previous tile version

#### Chunk Auto-Render
- `ChunkLoadListener` hooks `ServerChunkEvents.CHUNK_LOAD`
- Debounce: tiles rendered 5 s after last chunk load in their area
- `chunkRenderDebounceMs` config parameter (default 5000 ms)

#### Commands (operator level 2)
- `/webmap status`, `/webmap render`, `/webmap render-area`, `/webmap fullrender`, `/webmap stoprender`, `/webmap reload`

#### Web Interface
- Leaflet 1.9.4, custom CRS, dimension switcher, player markers, dark theme
- Mobile bottom control panel, safe-area support, touch gestures

### Fixed (relative to Forge 0.2.0 — incorporated directly into this initial release)
- **Chunks disappearing during new-chunk generation** — new tiles rendered with `loadChunks=true` so data is read from disk before the debounce fires.
- **Existing tile erased by blank re-render** — renderer always composites over the previously saved tile.
- **All-transparent tile written to disk** — write skipped when output image has no opaque pixels.
- **Height-shading seam artifacts** — height shading from neighbor chunks only applied when neighbor data is confirmed loaded.
