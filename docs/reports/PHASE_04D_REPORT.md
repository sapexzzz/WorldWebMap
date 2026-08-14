# Phase 04D — verification-only completion

## 1. Canonical worktrees and baseline

- Fabric: `/home/mentality/Scripts/java-world-web-map`, branch `2.1fabric`, HEAD `d171c30ee664164745e6b6ed65be0e7a29018f15`.
- Forge: `/home/mentality/Scripts/java-world-web-map-forge-phase02b`, branch `2.1forge`, HEAD `bc1cd58de09661bcc197df9fb207a6002586b6e1`.
- No worktree was reset or recreated. Initial `git status --short` showed the accepted cumulative Phase 02–04 dirty state: modified production/config/resource files, plus untracked `docs/`, Phase 04 production classes, and `src/test/` in each loader (Forge also had deleted `gradlew.orig` and untracked `LICENSE`).

## 2. Production baseline confirmation

Confirmed in both loaders: Nether `SurfaceHeightResolver`; its use in `TileSnapshotBuilder` and `ApiBiomeHandler`; reversible custom-dimension codec; `TileStorage.forWorld` using the actual world path; `saveTilesInsideWorldFolder`; spawn-centered `fullrender`; corrupt PNG quarantine and partial/complete replacement policy; and `TileRenderer.Result` propagation with a WRITTEN-only success counter. No fixed biome Y=64 remains in the production lookup path.

## 3. Exact regression results

The fresh complete suite passed in Fabric (73 tests) and Forge (72 tests). The following tests were present and PASS in both loaders unless otherwise stated:

- Dimension codec: `vanillaDimensionNamesRemainStable`, `customDimensionEncodingIsReversible`, `customDimensionsWithSamePathDoNotCollide`, `malformedCustomDimensionEncodingIsRejected`, `unknownDimensionDoesNotFallbackToOverworld`, `encodedDimensionCannotEscapeTileRoot` — PASS. Reversible inputs include `moda:moon`, `modb:moon`, and `moda:space/moon`.
- Surface resolver: `netherSurfaceIgnoresBedrockRoof`, `netherSurfaceFindsPlayableTerrainBelowRoof`, `netherSurfaceHandlesLavaOcean`, `netherSurfaceHandlesOpenCavern`, `overworldSurfaceBehaviorUnchanged`, `endSurfaceBehaviorUnchanged` — PASS.
- Biome surface-Y path: `biomeLookupUsesResolvedSurfaceY`, `overworldBiomeLookupUsesSurfaceHeight`, `netherBiomeLookupUsesPlayableSurface`, `endBiomeLookupUsesSurfaceHeight` — PASS.
- Corrupt tiles using real invalid PNG bytes and temporary directories: `corruptExistingTileDoesNotSilentlyDestroyPartialData`, `partialSnapshotSkipsCorruptTileReplacement`, `completeSnapshotCanReplaceCorruptTile`, `corruptTileQuarantineNameIsUnique`, `corruptTileFailureLeavesNoTempFiles` — PASS.
- Render results/metrics: `successfulRenderIncrementsSuccessMetric`, `failedRenderIsNotCountedAsSuccess`, `emptyRenderIsNotCountedAsWritten`, `corruptPartialSkipIsNotCountedAsSuccess`, `rendererResultDistinguishesWrittenAndSkipped` — PASS. There is no `UNCHANGED` result; its metric semantics are therefore not applicable.
- Production storage construction through `TileStorage.forWorld`: `productionStorageUsesActualWorldPath`, `productionStorageHonorsInsideWorldFlag`, `productionStorageHonorsOutsideWorldFlag` — PASS. These verify custom world root, contained configured subdirectory, and distinct inside/outside roots.
- Spawn conversion: `fullRenderUsesActualSpawn` — PASS for `(0,0)`, `(255,255)`, `(256,256)`, `(-1,-1)`, `(-256,-256)`, and `(1024,-512)`.

## 4. Test accounting and files changed

Phase 04 verification test classes are `SurfaceHeightResolverTest`, `BiomeSurfaceYTest`, `DimensionUtilCodecTest`, `TileStorageTempFileTest`, `TileRootResolverTest`, `TileRenderManagerChunkStateTest`, and `FullRenderCenterTest`, alongside the existing Phase 02/03 regression classes. All earlier Phase 02 and Phase 03 regressions are included in, and passed with, the complete suites.

Phase 04D changes are limited to the reports, the above regression tests, and small testability/parity seams in `SurfaceHeightResolver`, Forge `DimensionUtil`, and Forge `TileSnapshot`/`TileRenderer`; no scheduler, UI, dependency, CI, licensing, release, or performance work was changed.

## 5. Clean builds

Executed exactly:

```sh
cd /home/mentality/Scripts/java-world-web-map
PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build
cd /home/mentality/Scripts/java-world-web-map-forge-phase02b
PATH="/usr/lib/jvm/java-17-openjdk/bin:$PATH" ./gradlew --no-daemon --console=plain clean check build
```

Fabric: PASS. Forge: PASS.

## 6. Built JAR metadata

Commands used:

```sh
unzip -p build/libs/fabricwebmap-0.2.1.jar fabric.mod.json
unzip -p /home/mentality/Scripts/java-world-web-map-forge-phase02b/build/libs/forgewebmap-0.2.1.jar META-INF/mods.toml
```

- Fabric JAR: version `0.2.1`; license `CC0-1.0`; Fabric Loader `>=0.15.11` (unchanged accepted baseline); Minecraft `~1.20.1`.
- Forge JAR: version `0.2.1`; license `CC0-1.0`; Forge `[47.2.0,47.2.0]`; Minecraft `[1.20.1,1.20.1]`.

## 7. Final validation, parity, and risks

This report was refreshed after the final clean builds and JAR inspection. `git diff --check` was then run in both canonical worktrees: PASS in Fabric and PASS in Forge.

Fabric/Forge parity is confirmed for the Phase 04 behavior and required verification suites. Remaining risk is limited to true live-Minecraft integration: real server lifecycle/world-path behavior, registry/biome access, and on-disk rendering under a running game server.

PHASE 04 COMPLETE — READY FOR PHASE 05
