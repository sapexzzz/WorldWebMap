package com.mentality.forgewebmap.storage;

import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.util.DimensionUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

/**
 * Manages tile files on disk.
 *
 * Layout:
 *   <tilesRoot>/<dimension>/<zoom>/<x>_<z>.png
 *
 * Where tilesRoot defaults to:
 *   world/forgewebmap/tiles/
 */
public class TileStorage {

    private final Path tilesRoot;
    // CRC32 of the last successfully written tile image (in-memory cache).
    // Key: "dim/zoom/x_z". Prevents overwriting unchanged tiles and keeps
    // the old file intact while a new one is being prepared.
    private final ConcurrentHashMap<String, Long> tileChecksums = new ConcurrentHashMap<>();

    public TileStorage(WebMapConfig config) {
        // Tiles are stored inside the world folder (current working directory / world)
        // CWD for a Forge dedicated server is the server root dir.
        this(config, Paths.get("").toAbsolutePath(), Paths.get("world").toAbsolutePath());
    }
    public TileStorage(WebMapConfig config, Path serverRoot, Path worldRoot) {
        this.tilesRoot = TileRootResolver.resolve(serverRoot, worldRoot, config.isSaveTilesInsideWorldFolder(), config.getTilesDirectory());
        try {
            Files.createDirectories(tilesRoot);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create tiles directory: " + tilesRoot, e);
        }
    }
    public static TileStorage forWorld(WebMapConfig config, Path worldRoot) { Path parent=worldRoot.getParent(); return new TileStorage(config,parent==null?Paths.get(""):parent,worldRoot); }

    public Path getTilePath(String dimension, int zoom, int x, int z) {
        // Safe dimension name (prevent path traversal)
        String safeDim = sanitizeName(dimension);
        return tilesRoot
                .resolve(safeDim)
                .resolve(String.valueOf(zoom))
                .resolve(x + "_" + z + ".png");
    }

    public Path getTilePath(ResourceKey<Level> dimension, int zoom, int x, int z) {
        return getTilePath(DimensionUtil.toWebName(dimension), zoom, x, z);
    }

    public boolean exists(String dimension, int zoom, int x, int z) {
        return Files.exists(getTilePath(dimension, zoom, x, z));
    }

    public boolean exists(ResourceKey<Level> dimension, int zoom, int x, int z) {
        return Files.exists(getTilePath(dimension, zoom, x, z));
    }

    public void write(String dimension, int zoom, int x, int z, BufferedImage image) throws IOException {
        Path path = getTilePath(dimension, zoom, x, z);
        Files.createDirectories(path.getParent());

        // 1. Encode the PNG into memory so we can compute a checksum before touching disk.
        byte[] pngBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(32768)) {
            ImageIO.write(image, "PNG", baos);
            pngBytes = baos.toByteArray();
        }

        // 2. Compare checksum with last write — skip if identical (tile unchanged).
        String cacheKey = dimension + "/" + zoom + "/" + x + "_" + z;
        CRC32 crc = new CRC32();
        crc.update(pngBytes);
        long newChecksum = crc.getValue();
        Long oldChecksum = tileChecksums.get(cacheKey);
        if (oldChecksum != null && oldChecksum == newChecksum) {
            return; // nothing changed, keep existing file untouched
        }

        // 3. Write to a temp file first, then atomically replace the target.
        //    This ensures the HTTP handler never reads a half-written PNG.
        Path tmp = Files.createTempFile(path.getParent(), x + "_" + z + "_", ".tmp");
        try {
            Files.write(tmp, pngBytes);
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                // Fallback for filesystems that don't support atomic move
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            Files.deleteIfExists(tmp);
            throw e;
        }

        tileChecksums.put(cacheKey, newChecksum);
    }

    public void write(ResourceKey<Level> dimension, int zoom, int x, int z, BufferedImage image) throws IOException {
        write(DimensionUtil.toWebName(dimension), zoom, x, z, image);
    }

    public BufferedImage read(String dimension, int zoom, int x, int z) throws IOException {
        Path path = getTilePath(dimension, zoom, x, z);
        return ImageIO.read(path.toFile());
    }
    public Path quarantine(String dimension,int zoom,int x,int z)throws IOException{Path source=getTilePath(dimension,zoom,x,z);return Files.move(source,source.resolveSibling(source.getFileName()+".corrupt."+java.util.UUID.randomUUID()));}

    /**
     * Returns the tiles root directory (used by HTTP handler for serving files).
     */
    public Path getTilesRoot() {
        return tilesRoot;
    }

    /**
     * Returns total number of rendered PNG files across all dimensions/zooms.
     */
    public long countTiles() {
        if (!Files.exists(tilesRoot)) return 0;
        try (var stream = Files.walk(tilesRoot)) {
            return stream.filter(p -> p.toString().endsWith(".png")).count();
        } catch (IOException e) {
            return 0;
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Strip any path separators / special chars from a dimension name.
     * Only alphanumerics and underscores allowed.
     */
    private static String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
