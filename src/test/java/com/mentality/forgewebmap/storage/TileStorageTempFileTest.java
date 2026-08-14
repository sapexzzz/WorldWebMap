package com.mentality.forgewebmap.storage;

import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.render.TileRenderer;
import com.mentality.forgewebmap.render.TileSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TileStorageTempFileTest {
    @TempDir Path dir;
    private TileStorage storage() throws Exception { WebMapConfig c = new WebMapConfig(dir.resolve("config")); c.load(); return TileStorage.forWorld(c, dir.resolve("world")); }
    private TileSnapshot snapshot(boolean complete) { return new TileSnapshot("overworld", 0, 0, 0, 1, new int[]{0xff00ff00}, new int[]{64}, new boolean[]{complete}); }
    @Test void temporaryFilesAreUniqueAndCleanable() throws Exception { Path a = Files.createTempFile(dir, "tile_", ".tmp"), b = Files.createTempFile(dir, "tile_", ".tmp"); assertNotEquals(a, b); assertEquals(dir, a.getParent()); Files.delete(a); Files.delete(b); assertEquals(0, Files.list(dir).count()); }
    @Test void corruptExistingTileDoesNotSilentlyDestroyPartialData() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1,2,3}); assertEquals(TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL,new TileRenderer(s).render(snapshot(false))); assertFalse(Files.exists(p)); assertEquals(1,Files.list(p.getParent()).filter(x->x.getFileName().toString().contains(".corrupt.")).count()); }
    @Test void partialSnapshotSkipsCorruptTileReplacement() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1,2,3}); assertEquals(TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL,new TileRenderer(s).render(snapshot(false))); }
    @Test void completeSnapshotCanReplaceCorruptTile() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1,2,3}); assertEquals(TileRenderer.Result.WRITTEN,new TileRenderer(s).render(snapshot(true))); assertNotNull(javax.imageio.ImageIO.read(p.toFile())); }
    @Test void corruptTileQuarantineNameIsUnique() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1}); Path a=s.quarantine("overworld",0,0,0); Files.write(p,new byte[]{2}); Path b=s.quarantine("overworld",0,0,0); assertNotEquals(a,b); }
    @Test void corruptTileFailureLeavesNoTempFiles() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1}); new TileRenderer(s).render(snapshot(false)); assertEquals(0,Files.list(p.getParent()).filter(x->x.getFileName().toString().endsWith(".tmp")).count()); }
}
