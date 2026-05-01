package com.mentality.fabricwebmap.storage;

import com.mentality.fabricwebmap.config.WebMapConfig;
import com.mentality.fabricwebmap.util.DimensionUtil;
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
 *   world/fabricwebmap/tiles/
 */
public class TileStorage {

    private final Path tilesRoot;
    private final ConcurrentHashMap<String, Long> tileChecksums = new ConcurrentHashMap<>();

    public TileStorage(WebMapConfig config) {
        this.tilesRoot = Paths.get("world", config.getTilesDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(tilesRoot);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create tiles directory: " + tilesRoot, e);
        }
    }

    public Path getTilePath(String dimension, int zoom, int x, int z) {
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

        byte[] pngBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(32768)) {
            ImageIO.write(image, "PNG", baos);
            pngBytes = baos.toByteArray();
        }

        String cacheKey = dimension + "/" + zoom + "/" + x + "_" + z;
        CRC32 crc = new CRC32();
        crc.update(pngBytes);
        long newChecksum = crc.getValue();
        Long oldChecksum = tileChecksums.get(cacheKey);
        if (oldChecksum != null && oldChecksum == newChecksum) {
            return;
        }

        Path tmp = path.resolveSibling(x + "_" + z + ".tmp");
        try {
            Files.write(tmp, pngBytes);
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
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

    public Path getTilesRoot() {
        return tilesRoot;
    }

    public long countTiles() {
        if (!Files.exists(tilesRoot)) return 0;
        try (var stream = Files.walk(tilesRoot)) {
            return stream.filter(p -> p.toString().endsWith(".png")).count();
        } catch (IOException e) {
            return 0;
        }
    }

    private static String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
