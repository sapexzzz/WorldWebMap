package com.mentality.fabricwebmap.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TileRenderManagerChunkStateTest {
    private TileRenderManager manager() throws Exception { java.nio.file.Path d=java.nio.file.Files.createTempDirectory("wm"); com.mentality.fabricwebmap.config.WebMapConfig c=new com.mentality.fabricwebmap.config.WebMapConfig(d); c.load(); return new TileRenderManager(c); }
    @Test void successfulRenderIncrementsSuccessMetric() throws Exception { TileRenderManager m=manager(); assertEquals(1,m.recordRenderResult(TileRenderer.Result.WRITTEN)); }
    @Test void failedRenderIsNotCountedAsSuccess() throws Exception { TileRenderManager m=manager(); m.recordRenderResult(TileRenderer.Result.FAILED); assertEquals(0,m.getRenderedTiles()); }
    @Test void emptyRenderIsNotCountedAsWritten() throws Exception { TileRenderManager m=manager(); m.recordRenderResult(TileRenderer.Result.EMPTY); assertEquals(0,m.getRenderedTiles()); }
    @Test void corruptPartialSkipIsNotCountedAsSuccess() throws Exception { TileRenderManager m=manager(); m.recordRenderResult(TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL); assertEquals(0,m.getRenderedTiles()); }
    @Test void rendererResultDistinguishesWrittenAndSkipped(){assertNotEquals(TileRenderer.Result.WRITTEN,TileRenderer.Result.EMPTY);assertNotEquals(TileRenderer.Result.EMPTY,TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL);assertNotEquals(TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL,TileRenderer.Result.FAILED);}
    @Test void disabledRenderManagerDoesNotAccumulateChunkEvents() {
        PendingChunkTracker<String> pending = new PendingChunkTracker<>();
        pending.recordIfRunning(false,"idle",()->"idle",ignored -> {}); assertEquals(0,pending.size());
        pending.recordIfRunning(false,"stopped",()->"stopped",ignored -> {}); assertEquals(0,pending.size());
        pending.recordIfRunning(true,"running",()->"running",ignored -> {}); assertEquals(1,pending.size());
        pending.clear(); pending.recordIfRunning(false,"after-stop",()->"after-stop",ignored -> {}); assertEquals(0,pending.size());
    }
}
