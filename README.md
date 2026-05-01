# World Web Map (Fabric)

Server-side Fabric 1.20.1 mod that renders a 2D top-down map of your Minecraft world and serves it in a browser.

> **Current version: 0.2.0**  
> See [CHANGELOG.md](CHANGELOG.md) for full history.  
> Forge version available at [../forge/](../forge/).

---

## 1. What is this?

FabricWebMap installs on a Minecraft Fabric dedicated server, starts an embedded HTTP server, and shows a zoomable/pannable 2D map of your world at:

```
http://server-ip:8123
```

No client mods required. Players just open a browser.

---

## 2. Requirements

| Component | Version |
|-----------|---------|
| Minecraft | 1.20.1 |
| Fabric Loader | 0.15.11+ |
| Fabric API | 0.92.2+1.20.1 |
| Java | 17 |
| OS | Any (Linux recommended for servers) |

---

## 3. Installation

### Build from source

```bash
git clone <repo>
cd fabric
./gradlew build
```

The jar is produced at:
```
build/libs/fabricwebmap-0.2.0.jar
```

### Deploy

1. Copy `fabricwebmap-0.2.0.jar` into your server's `mods/` folder.
2. Make sure `fabric-api-*.jar` is also in `mods/`.
3. Start the server.
4. Open `http://server-ip:8123` in a browser.

You should see in server logs:
```
FabricWebMap started. Web map available at http://server-ip:8123
```

---

## 4. Commands

All commands require operator level 2.

| Command | Description |
|---------|-------------|
| `/webmap status` | Show web server status, queue size, rendered tiles count |
| `/webmap render <tileX> <tileZ>` | Render a single tile in Overworld (tile coords = blockCoord / 256) |
| `/webmap render-area <minX> <minZ> <maxX> <maxZ>` | Queue a rectangular area for render (max 10 000 tiles) |
| `/webmap fullrender <radius>` | Render a square around world spawn (e.g. radius 32 = 64×64 tiles) |
| `/webmap stoprender` | Clear the render queue |
| `/webmap reload` | Reload config; restarts web server if port/bind changed |

**Tile coordinates** vs **block coordinates**:
```
tileX = floor(blockX / 256)
tileZ = floor(blockZ / 256)
```
So `/webmap render 0 0` renders blocks 0–255 × 0–255.

---

## 5. Config

Config file: `<config-dir>/fabricwebmap-common.properties`  
(typically `config/fabricwebmap-common.properties` next to the server jar)  
Created automatically on first run.

| Key | Default | Description |
|-----|---------|-------------|
| `enabled` | `true` | Enable the mod |
| `bindAddress` | `0.0.0.0` | IP to bind HTTP server. **Warning:** `0.0.0.0` makes the map reachable from outside if the port is open. Use `127.0.0.1` for local-only. |
| `port` | `8123` | HTTP port |
| `tileSize` | `256` | Pixels per tile (also blocks per tile) |
| `renderThreads` | `1` | Worker threads for PNG generation |
| `maxTilesPerTick` | `1` | Max tiles sampled per server tick (keep low to avoid lag) |
| `ticksBetweenRenders` | `20` | Server ticks between queue processing steps |
| `enablePlayerMarkers` | `true` | Show players on map |
| `enableAutoRender` | `true` | Auto-render chunks when they load |
| `chunkRenderDebounceMs` | `5000` | Debounce delay before rendering a newly loaded tile (ms) |
| `renderRadiusAroundPlayers` | `4` | Tiles around each player for auto-render |
| `saveTilesInsideWorldFolder` | `true` | Save tiles inside world/ folder |
| `tilesDirectory` | `fabricwebmap/tiles` | Relative path for tiles |
| `logRenderProgress` | `true` | Log every 50 tiles rendered |

---

## 6. Where are tiles stored?

```
world/fabricwebmap/tiles/
├── overworld/
│   └── 0/
│       ├── 0_0.png
│       ├── 1_0.png
│       └── ...
├── the_nether/
│   └── 0/
└── the_end/
    └── 0/
```

File naming: `<tileX>_<tileZ>.png`

---

## 7. API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Web map frontend (index.html) |
| `GET` | `/tiles/{dim}/{zoom}/{x}/{z}.png` | PNG tile image |
| `GET` | `/api/status` | JSON: mod status, queue size, tile count |
| `GET` | `/api/players` | JSON: online players with coords and dimension |
| `GET` | `/api/config` | JSON: tileSize, zoom range, available dimensions |
| `GET` | `/api/biome?dim=overworld&x=100&z=200` | JSON: biome name and registry ID |

---

## 8. Differences from the Forge version

| Aspect | Forge | Fabric |
|--------|-------|--------|
| Loader | Forge 47.x | Fabric Loader 0.15.11 |
| Config dir | `config/` (Forge) | `config/` (via FabricLoader API) |
| Events | `@SubscribeEvent` / `@Mod.EventBusSubscriber` | `ServerLifecycleEvents`, `ServerTickEvents`, `ServerChunkEvents`, `CommandRegistrationCallback` |
| Mod init | `@Mod` class | `ModInitializer.onInitialize()` |
| Config file | `forgewebmap-common.properties` | `fabricwebmap-common.properties` |
| Tile path | `world/forgewebmap/tiles/` | `world/fabricwebmap/tiles/` |
| Jar name | `forgewebmap-0.2.0.jar` | `fabricwebmap-0.2.0.jar` |
| Extra dep | none | `fabric-api` |

Functionality is identical: same HTTP server, same render pipeline, same web UI, same commands.

---

## 9. Known Limitations

- **2D only** — top-down flat map, no 3D or isometric view
- **Simple block colors** — no resource pack textures, no biome tinting yet
- **No cave map** — only world surface (WORLD_SURFACE heightmap)
- **Unloaded chunks** show as dark gray on first render — but previously rendered areas are always preserved via compositing
- **No authentication** — anyone with network access to port 8123 can view the map
- **No HTTPS** — plain HTTP only

---

## 10. Roadmap

- [x] Biome name display on hover
- [x] Chunks no longer disappear during new-chunk generation or re-renders
- [ ] Biome color tinting (grass/water/leaves)
- [ ] HTTPS support
- [ ] Authentication (password-protected map)
- [ ] Nether/End render commands
- [ ] Better water rendering (depth-based tinting)
- [ ] Cave map (secondary layer)
- [ ] Player markers with skin heads
- [ ] World border overlay
- [ ] WebSocket live tile updates
- [ ] Multiple zoom levels
