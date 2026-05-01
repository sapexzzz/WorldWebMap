# World Web Map (Forge)

Server-side Forge 1.20.1 mod that renders a 2D top-down map of your Minecraft world and serves it in a browser.

> **Current version: 0.2.1**  
> See [CHANGELOG.md](CHANGELOG.md) for full history.

---

## 1. What is this?

ForgeWebMap installs on a Minecraft Forge dedicated server, starts an embedded HTTP server, and shows a zoomable/pannable 2D map of your world at:

```
http://server-ip:8123
```

No client mods required. Players just open a browser.

---

## 2. Requirements

| Component | Version |
|-----------|---------|
| Minecraft | 1.20.1 |
| Forge | 47.x (tested on 47.2.0) |
| Java | 17 |
| OS | Any (Linux recommended for servers) |

---

## 3. Installation

### Build from source

```bash
git clone <repo>
cd forgewebmap
./gradlew build
```

The jar is produced at:
```
build/libs/forgewebmap-0.2.0.jar
```

### Deploy

1. Copy `forgewebmap-0.2.0.jar` into your server's `mods/` folder.
2. Start the server.
3. Open `http://server-ip:8123` in a browser.

You should see in server logs:
```
ForgeWebMap started. Web map available at http://server-ip:8123
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

Config file: `config/forgewebmap-common.properties`  
Created automatically on first run.

| Key | Default | Description |
|-----|---------|-------------|
| `enabled` | `true` | Enable the mod |
| `bindAddress` | `0.0.0.0` | IP to bind HTTP server. **Warning:** `0.0.0.0` makes the map reachable from outside if the port is open. Use `127.0.0.1` for local-only. |
| `port` | `8123` | HTTP port |
| `tileSize` | `256` | Pixels per tile (also blocks per tile) |
| `renderThreads` | `1` | Worker threads for PNG generation |
| `maxTilesPerTick` | `1` | Max tiles sampled per server tick (keep low to avoid lag) |
| `enablePlayerMarkers` | `true` | Show players on map |
| `enableAutoRender` | `false` | Auto-render chunks around players (not yet implemented) |
| `renderRadiusAroundPlayers` | `4` | Tiles around each player for auto-render |
| `saveTilesInsideWorldFolder` | `true` | Save tiles inside world/ folder |
| `tilesDirectory` | `forgewebmap/tiles` | Relative path for tiles |
| `logRenderProgress` | `true` | Log every 50 tiles rendered |

---

## 6. Where are tiles stored?

```
world/forgewebmap/tiles/
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

## 7. How to render an area

For a 1km × 1km area around spawn (blocks -512 to 512):
```
/webmap render-area -2 -2 2 2
```

For a larger area (blocks -5120 to 5120 = tiles -20 to 20):
```
/webmap fullrender 20
```

Monitor progress:
```
/webmap status
```

Stop if needed:
```
/webmap stoprender
```

---

## 8. Known Limitations

- **2D only** — top-down flat map, no 3D or isometric view
- **Simple block colors** — no resource pack textures, no biome tinting yet
- **No cave map** — only world surface (WORLD_SURFACE heightmap)
- **Unloaded chunks** show as dark gray — tiles are only rendered from loaded chunks
- **No authentication** — anyone with network access to port 8123 can view the map
- **No HTTPS** — plain HTTP only
- **Overworld only** for render commands (Nether/End architecture is ready but not exposed in MVP commands)
- **fullrender** only renders area around spawn, not all existing region files

---

## 9. Roadmap

- [x] Biome name display on hover (added in 0.2.0)
- [x] Chunks no longer disappear during new-chunk generation or re-renders (fixed in 0.2.1)
- [ ] Biome color tinting (grass/water/leaves)
- [ ] HTTPS support
- [ ] Authentication (password-protected map)
- [ ] Nether/End render commands
- [ ] Better water rendering (depth-based tinting)
- [ ] Cave map (secondary layer)
- [ ] Player markers with skin heads
- [ ] World border overlay
- [ ] Land claims / region overlay
- [ ] WebSocket live tile updates
- [ ] Isometric render mode
- [ ] BlueMap-style 3D mode
- [ ] Auto-render chunks as players explore
- [ ] Multiple zoom levels
