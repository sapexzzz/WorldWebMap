package com.mentality.forgewebmap.render;
import com.mentality.forgewebmap.config.WebMapConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TileRenderManagerChunkStateTest {
 private TileRenderManager manager()throws Exception{java.nio.file.Path d=java.nio.file.Files.createTempDirectory("wm");WebMapConfig c=new WebMapConfig(d);c.load();return new TileRenderManager(c);}
 @Test void successfulRenderIncrementsSuccessMetric()throws Exception{assertEquals(1,manager().recordRenderResult(TileRenderer.Result.WRITTEN));}
 @Test void failedRenderIsNotCountedAsSuccess()throws Exception{TileRenderManager m=manager();m.recordRenderResult(TileRenderer.Result.FAILED);assertEquals(0,m.getRenderedTiles());}
 @Test void emptyRenderIsNotCountedAsWritten()throws Exception{TileRenderManager m=manager();m.recordRenderResult(TileRenderer.Result.EMPTY);assertEquals(0,m.getRenderedTiles());}
 @Test void corruptPartialSkipIsNotCountedAsSuccess()throws Exception{TileRenderManager m=manager();m.recordRenderResult(TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL);assertEquals(0,m.getRenderedTiles());}
 @Test void rendererResultDistinguishesWrittenAndSkipped(){assertNotEquals(TileRenderer.Result.WRITTEN,TileRenderer.Result.EMPTY);assertNotEquals(TileRenderer.Result.EMPTY,TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL);assertNotEquals(TileRenderer.Result.SKIPPED_CORRUPT_PARTIAL,TileRenderer.Result.FAILED);}
 @Test void disabledRenderManagerDoesNotAccumulateChunkEvents(){PendingChunkTracker<String> p=new PendingChunkTracker<>();p.recordIfRunning(false,"idle",()->"idle",ignored->{});assertEquals(0,p.size());p.recordIfRunning(true,"running",()->"running",ignored->{});assertEquals(1,p.size());}
}
