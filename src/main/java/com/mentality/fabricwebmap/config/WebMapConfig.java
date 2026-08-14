package com.mentality.fabricwebmap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Simple config backed by java.util.Properties.
 * File: config/fabricwebmap-common.properties
 *
 * WARNING: bindAddress = "0.0.0.0" makes the web map accessible from outside
 * the machine if the port is open in your firewall. Restrict it if needed.
 */
public class WebMapConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("fabricwebmap");

    private final Path configFile;

    // Defaults
    private boolean enabled = true;
    private String bindAddress = "0.0.0.0";
    private int port = 8123;
    private int tileSize = 256;
    private int renderThreads = 1;
    private int maxTilesPerTick = 1;
    private boolean enablePlayerMarkers = true;
    private boolean enableAutoRender = true;
    private int renderRadiusAroundPlayers = 4;
    private boolean saveTilesInsideWorldFolder = true;
    private String tilesDirectory = "fabricwebmap/tiles";
    private String webDirectory = "fabricwebmap/web";
    private boolean logRenderProgress = true;
    private int chunkRenderDebounceMs = 5000;
    private int ticksBetweenRenders = 20;
    private int maxSnapshotMillisPerTick = 10;

    public WebMapConfig(Path configDir) {
        this.configFile = configDir.resolve("fabricwebmap-common.properties");
    }

    public void load() {
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
                save();
            } catch (IOException e) {
                LOGGER.error("Failed to create FabricWebMap config file", e);
            }
            return;
        }

        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(configFile)) {
            props.load(reader);
        } catch (IOException e) {
            LOGGER.error("Failed to read FabricWebMap config, using defaults", e);
            return;
        }

        try {
        enabled = bool(props, "enabled", enabled);
        bindAddress = str(props, "bindAddress", bindAddress);
        port = intVal(props, "port", port);
        tileSize = intVal(props, "tileSize", tileSize);
        renderThreads = intVal(props, "renderThreads", renderThreads);
        maxTilesPerTick = intVal(props, "maxTilesPerTick", maxTilesPerTick);
        enablePlayerMarkers = bool(props, "enablePlayerMarkers", enablePlayerMarkers);
        enableAutoRender = bool(props, "enableAutoRender", enableAutoRender);
        renderRadiusAroundPlayers = intVal(props, "renderRadiusAroundPlayers", renderRadiusAroundPlayers);
        saveTilesInsideWorldFolder = bool(props, "saveTilesInsideWorldFolder", saveTilesInsideWorldFolder);
        tilesDirectory = str(props, "tilesDirectory", tilesDirectory);
        webDirectory = str(props, "webDirectory", webDirectory);
        logRenderProgress = bool(props, "logRenderProgress", logRenderProgress);
        chunkRenderDebounceMs = intVal(props, "chunkRenderDebounceMs", chunkRenderDebounceMs);
        ticksBetweenRenders = intVal(props, "ticksBetweenRenders", ticksBetweenRenders);
        maxSnapshotMillisPerTick = intVal(props, "maxSnapshotMillisPerTick", maxSnapshotMillisPerTick);
        validate();
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid World Web Map configuration in {}: {}. Using defaults.", configFile, e.getMessage());
            resetToDefaults();
            return;
        }

        LOGGER.info("World Web Map config loaded from {}", configFile);
    }

    public void save() throws IOException {
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(enabled));
        props.setProperty("bindAddress", bindAddress);
        props.setProperty("port", String.valueOf(port));
        props.setProperty("tileSize", String.valueOf(tileSize));
        props.setProperty("renderThreads", String.valueOf(renderThreads));
        props.setProperty("maxTilesPerTick", String.valueOf(maxTilesPerTick));
        props.setProperty("enablePlayerMarkers", String.valueOf(enablePlayerMarkers));
        props.setProperty("enableAutoRender", String.valueOf(enableAutoRender));
        props.setProperty("renderRadiusAroundPlayers", String.valueOf(renderRadiusAroundPlayers));
        props.setProperty("saveTilesInsideWorldFolder", String.valueOf(saveTilesInsideWorldFolder));
        props.setProperty("tilesDirectory", tilesDirectory);
        props.setProperty("webDirectory", webDirectory);
        props.setProperty("logRenderProgress", String.valueOf(logRenderProgress));
        props.setProperty("chunkRenderDebounceMs", String.valueOf(chunkRenderDebounceMs));
        props.setProperty("ticksBetweenRenders", String.valueOf(ticksBetweenRenders));
        props.setProperty("maxSnapshotMillisPerTick", String.valueOf(maxSnapshotMillisPerTick));

        try (Writer writer = Files.newBufferedWriter(configFile)) {
            props.store(writer,
                "World Web Map Configuration\n" +
                "WARNING: bindAddress=0.0.0.0 makes the web map reachable from outside\n" +
                "if this port is open in your firewall. Change to 127.0.0.1 for local-only access.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v != null ? Boolean.parseBoolean(v.trim()) : def;
    }

    private static int intVal(Properties p, String key, int def) {
        try {
            String v = p.getProperty(key);
            return v != null ? Integer.parseInt(v.trim()) : def;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be an integer", e);
        }
    }

    private static String str(Properties p, String key, String def) {
        String v = p.getProperty(key);
        return v != null ? v.trim() : def;
    }

    private void validate() {
        if (port < 1 || port > 65535) throw new IllegalArgumentException("port must be between 1 and 65535");
        if (tileSize != 256) throw new IllegalArgumentException("tileSize must be 256 until dynamic frontend tiles are implemented");
        if (renderThreads < 1 || renderThreads > 64) throw new IllegalArgumentException("renderThreads must be between 1 and 64");
        if (maxTilesPerTick < 1 || maxTilesPerTick > 1000) throw new IllegalArgumentException("maxTilesPerTick must be between 1 and 1000");
        if (ticksBetweenRenders < 1 || ticksBetweenRenders > 1200) throw new IllegalArgumentException("ticksBetweenRenders must be between 1 and 1200");
        if (chunkRenderDebounceMs < 0 || chunkRenderDebounceMs > 600000) throw new IllegalArgumentException("chunkRenderDebounceMs must be between 0 and 600000");
        if (maxSnapshotMillisPerTick < 1 || maxSnapshotMillisPerTick > 1000) throw new IllegalArgumentException("maxSnapshotMillisPerTick must be between 1 and 1000");
        validateDirectory("tilesDirectory", tilesDirectory);
        validateDirectory("webDirectory", webDirectory);
    }

    private static void validateDirectory(String key, String directory) {
        if (directory == null || directory.isBlank()) throw new IllegalArgumentException(key + " must not be empty");
        Path path = Paths.get(directory);
        if (path.isAbsolute() || path.normalize().startsWith("..")) throw new IllegalArgumentException(key + " must be a relative directory inside the server root");
    }

    private void resetToDefaults() {
        enabled = true; bindAddress = "0.0.0.0"; port = 8123; tileSize = 256; renderThreads = 1;
        maxTilesPerTick = 1; enablePlayerMarkers = true; enableAutoRender = true; renderRadiusAroundPlayers = 4;
        saveTilesInsideWorldFolder = true; tilesDirectory = "fabricwebmap/tiles"; webDirectory = "fabricwebmap/web";
        logRenderProgress = true; chunkRenderDebounceMs = 5000; ticksBetweenRenders = 20; maxSnapshotMillisPerTick = 10;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isEnabled() { return enabled; }
    public String getBindAddress() { return bindAddress; }
    public int getPort() { return port; }
    public int getTileSize() { return tileSize; }
    public int getRenderThreads() { return renderThreads; }
    public int getMaxTilesPerTick() { return maxTilesPerTick; }
    public boolean isEnablePlayerMarkers() { return enablePlayerMarkers; }
    public boolean isEnableAutoRender() { return enableAutoRender; }
    public int getRenderRadiusAroundPlayers() { return renderRadiusAroundPlayers; }
    public boolean isSaveTilesInsideWorldFolder() { return saveTilesInsideWorldFolder; }
    public String getTilesDirectory() { return tilesDirectory; }
    public String getWebDirectory() { return webDirectory; }
    public boolean isLogRenderProgress() { return logRenderProgress; }
    public int getChunkRenderDebounceMs() { return chunkRenderDebounceMs; }
    public int getTicksBetweenRenders() { return ticksBetweenRenders; }
    /** Main-thread snapshot time budget in milliseconds, reset on every render tick. */
    public int getMaxSnapshotMillisPerTick() { return maxSnapshotMillisPerTick; }
}
