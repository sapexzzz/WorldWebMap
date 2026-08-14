package com.mentality.fabricwebmap.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import com.mentality.fabricwebmap.config.WebMapConfig;
import com.mentality.fabricwebmap.render.*;
import static org.junit.jupiter.api.Assertions.*;

class TileStorageTempFileTest {
    @TempDir Path dir;
    @Test void independentTemporaryFilesAreUniqueAndStayInDestinationDirectory() throws Exception {
        Path first = TileStorage.createTempFile(dir, "tile_");
        Path second = TileStorage.createTempFile(dir, "tile_");
        assertNotEquals(first, second); assertEquals(dir, first.getParent()); assertEquals(dir, second.getParent());
        Files.delete(first); Files.delete(second);
        assertEquals(0, Files.list(dir).count());
    }
    private TileStorage storage() throws Exception { WebMapConfig c=new WebMapConfig(dir.resolve("config")); c.load(); return TileStorage.forWorld(c,dir.resolve("world")); }
    private TileSnapshot snapshot(boolean complete) { return new TileSnapshot("overworld",0,0,0,1,new int[]{0xff00ff00},new int[]{64},new boolean[]{complete}); }
    @Test void corruptExistingTileDoesNotSilentlyDestroyPartialData() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1,2,3}); assertEquals(TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL,new TileRenderer(s).render(snapshot(false))); assertFalse(Files.exists(p)); assertEquals(1,Files.list(p.getParent()).filter(x->x.getFileName().toString().contains(".corrupt.")).count()); }
    @Test void partialSnapshotSkipsCorruptTileReplacement() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1,2,3}); assertEquals(TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL,new TileRenderer(s).render(snapshot(false))); }
    @Test void completeSnapshotCanReplaceCorruptTile() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1,2,3}); assertEquals(TileRenderer.Result.WRITTEN,new TileRenderer(s).render(snapshot(true))); assertNotNull(javax.imageio.ImageIO.read(p.toFile())); }
    @Test void corruptTileQuarantineNameIsUnique() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1}); Path one=s.quarantine("overworld",0,0,0); Files.write(p,new byte[]{2}); Path two=s.quarantine("overworld",0,0,0); assertNotEquals(one,two); assertTrue(Files.exists(one)); assertTrue(Files.exists(two)); }
    @Test void corruptTileFailureLeavesNoTempFiles() throws Exception { TileStorage s=storage(); Path p=s.getTilePath("overworld",0,0,0); Files.createDirectories(p.getParent()); Files.write(p,new byte[]{1}); new TileRenderer(s).render(snapshot(false)); assertEquals(0,Files.list(p.getParent()).filter(x->x.getFileName().toString().endsWith(".tmp")).count()); }
}
