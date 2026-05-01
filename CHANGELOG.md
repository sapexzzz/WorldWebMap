# World Web Map (Forge) — Changelog

## [0.2.1] — 2026-05-01

### Fixed
- **Chunks disappearing during new-chunk generation** — new tiles are now rendered with `loadChunks=true` so chunks are read from disk before the debounce fires and they get unloaded.
- **Existing tile erased by blank re-render** — the renderer now always composites over the previously saved tile, not only when chunks were explicitly unloaded.
- **All-transparent tile written to disk** — if the rendered image has no opaque pixels (all chunks unloaded and no existing tile), the write is skipped to prevent overwriting a good tile with an empty one.
- **Height-shading seam artifacts** — height shading from a neighbor chunk now only applies when that neighbor's data is confirmed loaded; unloaded neighbors no longer contribute a spurious height of 64.

---

## [0.2.0] — 2026-04-30

### Added

#### Biome display
- New API endpoint `GET /api/biome?dim=overworld&x=100&z=200` — returns biome name and registry ID for any block coordinate
- Biome name shown on desktop toolbar next to coordinates (🌳 Dark Forest)
- Biome name shown on mobile bottom bar below coordinates
- Biome updates instantly as the cursor moves to a different block (no artificial delay)
- Vanilla biomes formatted as human-readable names (`minecraft:dark_forest` → `Dark Forest`)
- Modded biomes prefixed with mod name (`somemod:custom_biome` → `[somemod] Custom Biome`)

### Fixed
- Biome label was not updating during mouse movement (only after a 150 ms debounce that kept resetting). Now updates on every block change without delay.

---

## [0.1.0] — 2026-04-29

### Added

#### Web Server
- Built-in HTTP server based on `com.sun.net.httpserver.HttpServer`, port 8123
- Serving static files (index.html, app.js, style.css) from classpath
- Endpoint `/tiles/{dimension}/{zoom}/{x}/{z}.png` — Serving PNG map tiles
- API `/api/status` — Mod status, queue size, number of rendered tiles
- API `/api/players` — List of online players with coordinates and dimensions
- API `/api/config` — Tile size, zoom range, available dimensions

#### Map Renderer
- Render a 2D top view with height shading ((lighter) — higher, darker — lower)
- Snapshot system: world data is read on the main thread, PNG rendering is done in the background
- Dimension support: Overworld, Nether, The End
- Atomic tile writing (via temp file + `Files.move(ATOMIC_MOVE)`) — the browser never receives a corrupted PNG
- CRC32 change check — if the tile hasn't changed, the file is not overwritten
- Compositing: unloaded chunks take pixels from the previous version of the tile during re-rendering; already rendered areas never disappear
- Color palette for 60+ block types (`BlockColorResolver`)

#### Automatic rendering when loading chunks
- `ChunkLoadListener` listens to `ChunkEvent.Load` — new chunks are automatically added to the render queue
- Debounce system: tiles are rendered only via 5 seconds after the last chunk load in its area (avoids rendering with gray spots while the area is still loading)
- `chunkRenderDebounceMs` parameter (default 5000 ms)

#### Commands (for operators only, level 2)
- `/webmap status` — server, queue, and worker status
- `/webmap render <tileX> <tileZ>` — forced rendering of a single tile (loading chunks from disk)
- `/webmap render-area <minX> <minZ> <maxX> <maxZ>` — area render (max 10,000 tiles)
- `/webmap fullrender <radius>` — render a square around the spawn (radius 1–1000)
- `/webmap stoprender` — clear the render queue
- `/webmap reload` — reload the config without restarting Servers

#### Performance Optimizations
- Render worker threads run with priority 'MIN_PRIORITY'
- Tick throttling: the render queue is processed every N ticks (default 20 = 1 time/sec), parameter 'ticksBetweenRenders'
- Queue limit: maximum 100,000 tasks, de-replication by (dimension, tileX, tileZ, zoom)
- Team renderers load chunks from disk ('ChunkStatus.FULL'), automatic renderers do not

#### Configuration (`config/forgewebmap-common.properties`)
| Parameter | Default | Description |
|---|---|---|
| `enabled` | `true` | Enable mod |
| `bindAddress` | `0.0.0.0` | HTTP server address |
| `port` | `8123` | HTTP server port |
| `tileSize` | `256` | Tile size in blocks |
| `renderThreads` | `1` | Number of background rendering threads |
| `maxTilesPerTick` | `1` | Tiles processed per interval |
| `ticksBetweenRenders` | `20` | Ticks between queue processing |
| `enableAutoRender` | `true` | Render when loading chunks |
| `chunkRenderDebounceMs` | `5000` | Chunk debounce delay (ms) |
| `enablePlayerMarkers` | `true` | Player markers on the map |
| `logRenderProgress` | `true` | Log rendering progress |

#### Web Interface
- Leaflet 1.9.4, custom CRS (`scale(zoom) = 2^zoom`)
- Dimension switcher (Overworld / Nether / End)
- Display coordinates under cursor/finger
- Player markers with names, updated every 2 seconds
- Server status indicator (green/red)
- Pixelated tile rendering (`image-rendering: pixelated`)
- Dark theme

#### Mobile Adaptation
- Bottom control panel for screens ≤640px
- Safe-area support (notch, home indicator on iOS)
- Larger buttons for touchscreens (minimum 36–40px)
- `touch-action: none`, `overscroll-behavior: none` — gestures work correctly
- Disable browser scaling (`user-scalable=no`) in favor of zoom Leaflet
- Landscape mode support on phones
- Meta tags for adding to the desktop (iOS/Android)