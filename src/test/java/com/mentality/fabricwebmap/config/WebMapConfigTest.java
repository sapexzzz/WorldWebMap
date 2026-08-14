package com.mentality.fabricwebmap.config;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class WebMapConfigTest {
    @TempDir Path dir;
    @Test void invalidValuesResetToExplicitDefaults() throws Exception {
        Files.writeString(dir.resolve("fabricwebmap-common.properties"), "port=0\ntileSize=0\nrenderThreads=0\nmaxTilesPerTick=0\ntilesDirectory=\n");
        WebMapConfig config = new WebMapConfig(dir); config.load();
        assertEquals(8123, config.getPort()); assertEquals(256, config.getTileSize());
        assertEquals(1, config.getRenderThreads()); assertEquals(1, config.getMaxTilesPerTick());
        assertEquals("fabricwebmap/tiles", config.getTilesDirectory());
    }
    @Test void highPortAndMalformedIntegerResetToDefaults() throws Exception {
        Files.writeString(dir.resolve("fabricwebmap-common.properties"), "port=65536\nrenderThreads=not-a-number\nwebDirectory=\n");
        WebMapConfig config = new WebMapConfig(dir); config.load();
        assertEquals(8123, config.getPort()); assertEquals(1, config.getRenderThreads());
        assertEquals("fabricwebmap/web", config.getWebDirectory());
    }
    @Test void excessiveThreadsAndUnsupportedTileSizeResetToDefaults() throws Exception {
        Files.writeString(dir.resolve("fabricwebmap-common.properties"), "tileSize=128\nrenderThreads=65\n");
        WebMapConfig config = new WebMapConfig(dir); config.load();
        assertEquals(256, config.getTileSize()); assertEquals(1, config.getRenderThreads());
    }
    @Test void snapshotBudgetDefaultMinimumMaximumAndPersistence() throws Exception {
        WebMapConfig defaults = new WebMapConfig(dir); defaults.load(); assertEquals(10, defaults.getMaxSnapshotMillisPerTick());
        Files.writeString(dir.resolve("fabricwebmap-common.properties"), "maxSnapshotMillisPerTick=1\n"); WebMapConfig min = new WebMapConfig(dir); min.load(); assertEquals(1, min.getMaxSnapshotMillisPerTick()); min.save(); WebMapConfig reloaded = new WebMapConfig(dir); reloaded.load(); assertEquals(1, reloaded.getMaxSnapshotMillisPerTick());
        Files.writeString(dir.resolve("fabricwebmap-common.properties"), "maxSnapshotMillisPerTick=1000\n"); WebMapConfig max = new WebMapConfig(dir); max.load(); assertEquals(1000, max.getMaxSnapshotMillisPerTick());
    }
    @Test void malformedSnapshotBudgetResetsToDefault() throws Exception { Files.writeString(dir.resolve("fabricwebmap-common.properties"), "maxSnapshotMillisPerTick=bad\n"); WebMapConfig config=new WebMapConfig(dir); config.load(); assertEquals(10, config.getMaxSnapshotMillisPerTick()); }
}
