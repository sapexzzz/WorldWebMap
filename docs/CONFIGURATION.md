# Fabric configuration reference

File: `config/fabricwebmap-common.properties`. `/webmap reload` reads it. Invalid numeric/directory validation logs a diagnostic and resets the configuration object to defaults; booleans use Java parsing (`true` is true; other values are false). `enabled`, bind, and port apply through lifecycle reload; restart is safest for worker/storage-root changes.

| Key | Type/default | Valid values | Effect |
| --- | --- | --- | --- |
| `enabled` | boolean / `true` | boolean syntax | Enables services live. |
| `bindAddress` | string / `0.0.0.0` | address string | HTTP bind; live service replacement. |
| `port` | int / `8123` | 1–65535 | HTTP port; live service replacement. |
| `tileSize` | int / `256` | exactly 256 | Fixed native tile pixels/blocks. |
| `renderThreads` | int / `1` | 1–64 | PNG worker count; restart to rebuild pool. |
| `maxTilesPerTick` | int / `1` | 1–1000 | Snapshot cap per processing tick. |
| `maxSnapshotMillisPerTick` | int / `10` | 1–1000 ms | Main-thread snapshot budget. |
| `ticksBetweenRenders` | int / `20` | 1–1200 | Render processing interval. |
| `enablePlayerMarkers` | boolean / `true` | boolean syntax | Controls marker/API privacy. |
| `enableAutoRender` | boolean / `true` | boolean syntax | Enables chunk-load work. |
| `renderRadiusAroundPlayers` | int / `4` | integer; reserved | No active player-radius scheduler. |
| `saveTilesInsideWorldFolder` | boolean / `true` | boolean syntax | World root versus server root. |
| `tilesDirectory` | string / `fabricwebmap/tiles` | non-empty contained relative path | Tile subdirectory; restart safest after change. |
| `webDirectory` | string / `fabricwebmap/web` | non-empty contained relative path | Retained setting; packaged assets serve UI. |
| `logRenderProgress` | boolean / `true` | boolean syntax | Successful-render progress logs. |
| `chunkRenderDebounceMs` | int / `5000` | 0–600000 ms | Auto-render debounce. |
