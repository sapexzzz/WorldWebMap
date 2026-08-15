# Mentalitys | World Web Map — Forge

Server-side browser map for Minecraft **1.20.1** on Forge. No client mod is required. This branch retains the Forge build and runtime documentation; the Fabric implementation is maintained separately.

## Release status

- **0.2.2** is a release candidate and is not published yet.
- Latest published Forge release: [0.2.1](https://github.com/sqwiziiy/World-Web-Map/releases/tag/2.1forge).

## Links

- [GitHub Releases](https://github.com/sqwiziiy/World-Web-Map/releases)
- [Modrinth](https://modrinth.com/mod/mentalitys-world-web-map)
- [Issues](https://github.com/sqwiziiy/World-Web-Map/issues)
- [Fabric source branch](https://github.com/sqwiziiy/World-Web-Map/tree/2.2fabric)
- [Main project page](https://github.com/sqwiziiy/World-Web-Map)

## Supported version

Minecraft **1.20.1** · **Forge** · Java **17** · source branch [`2.2forge`](https://github.com/sqwiziiy/World-Web-Map/tree/2.2forge)

## Requirements and build

- Mod version `0.2.2` (release candidate; not yet published); Java 17; Minecraft 1.20.1
- Forge `47.2.0`; Gradle wrapper 8.1.1

```bash
git switch 2.2forge
./gradlew --version
./gradlew --no-daemon --console=plain clean check build
```

The primary development JAR is `build/libs/forgewebmap-0.2.2.jar`. Put it in the server `mods/` directory. Configuration is created at `config/forgewebmap-common.properties`.

## Operator commands

All commands require permission level 2.

| Command | Behavior |
| --- | --- |
| `/webmap status` | Shows service, queue, worker, successful-render, and fullrender state. |
| `/webmap reload` | Reloads configuration; enabled state is applied live and bind/port safely recreate HTTP service. |
| `/webmap render <tileX> <tileZ>` | HIGH-priority forced Overworld tile render; never generates or force-loads unexplored chunks. |
| `/webmap render-area <minX> <minZ> <maxX> <maxZ>` | NORMAL-priority Overworld area; maximum 10,000 tiles, without generating terrain. |
| `/webmap fullrender <radius>` | Spawn-centred LOW-priority lazy plan; radius 1000 is 4,004,001 logical tiles, not an immediate enqueue. |
| `/webmap stoprender` | Cancels fullrender and clears pending queue/chunk work; active atomic writes finish safely. |

Tile coordinates use `floor(block coordinate / 256)`, including negative coordinates. Fullrender respects bounded queue/admission and snapshot tick budgets; HIGH > NORMAL > LOW work is preferred.

## HTTP API and privacy

- `GET /` serves the map; `GET /tiles/{dimension}/{zoom}/{x}/{z}.png` serves tiles.
- `GET /api/status` includes `enabled`, `renderState`, `serverRunning`, `renderQueueSize`, `activeWorkers`, `renderedTiles` (WRITTEN results only), `fullRenderActive`, and `fullRenderRemaining`.
- `GET /api/config` exposes tile size, supported zoom range, and vanilla dimensions.
- `GET /api/players` returns `[]` when player markers are disabled—names, position, yaw, and dimension are not exposed.
- `GET /api/biome?dim=overworld&x=100&z=200` rejects unknown dimensions and may return 503 when unavailable; lookup is server-thread scheduled and uses rendering’s surface-selection concept.

Vanilla names are `overworld`, `the_nether`, and `the_end`. Custom dimensions are encoded as `custom-<base64url(resource-location)>`, preventing namespace collisions and unsafe path characters.

## Rendering, storage, and operational notes

Tiles are 256×256 PNGs. World sampling occurs on the server thread; PNG work occurs on bounded workers. Duplicate concurrent tile work is prevented; writes use temp files and atomic replacement. Corrupt PNGs are quarantined: partial snapshots do not replace them, while complete snapshots can rebuild them. Nether surfaces skip the bedrock roof.

With `saveTilesInsideWorldFolder=true`, tiles live at `<actual-world-root>/<tilesDirectory>/`; otherwise at `<server-root>/<tilesDirectory>/`. No `level-name=world` assumption is made. Rendering never generates unexplored chunks: unavailable terrain is skipped until normally generated/available. The checksum cache is process-memory-only, so an unchanged tile may be rewritten once after restart.

The current Leaflet JavaScript and CSS are loaded from the unpkg CDN. A browser therefore needs CDN/Internet access for the map UI unless those assets are locally bundled in a future release; this is a known deployment limitation.

## More documentation

- [Configuration reference](docs/CONFIGURATION.md)
- [CI and release validation](docs/CI.md)
- [Release provenance](docs/RELEASE_PROVENANCE.md)
- [Contributing](CONTRIBUTING.md)
