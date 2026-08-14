package com.mentality.forgewebmap.web;

import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.player.PlayerMarkerService;
import com.mentality.forgewebmap.render.TileRenderManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.concurrent.ThreadPoolExecutor;
import static org.junit.jupiter.api.Assertions.*;

class WebServerServiceTest {
    @TempDir Path dir;
    @Test void webServerStopShutsDownOwnedExecutor() throws Exception { WebServerService s=service(); s.start(); ThreadPoolExecutor e=s.ownedExecutorForTesting(); s.stop(); assertTrue(e.isShutdown()); assertTrue(e.isTerminated()); assertFalse(s.isRunning()); }
    @Test void webServerStopIsIdempotent() throws Exception { WebServerService s=service(); s.start(); s.stop(); assertDoesNotThrow(s::stop); assertFalse(s.isRunning()); }
    @Test void webServerRestartReplacesExecutor() throws Exception { WebServerService a=service(); a.start(); ThreadPoolExecutor first=a.ownedExecutorForTesting(); a.stop(); WebServerService b=service(); b.start(); ThreadPoolExecutor second=b.ownedExecutorForTesting(); assertTrue(first.isShutdown()); assertNotSame(first,second); assertFalse(second.isShutdown()); b.stop(); }
    private WebServerService service() throws Exception { java.nio.file.Files.writeString(dir.resolve("forgewebmap-common.properties"), "bindAddress=127.0.0.1\nport=0\n"); WebMapConfig c=new WebMapConfig(dir); c.load(); return new WebServerService(c,new TileRenderManager(c),new PlayerMarkerService()); }
}
