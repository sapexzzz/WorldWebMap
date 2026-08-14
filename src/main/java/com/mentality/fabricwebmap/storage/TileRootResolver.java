package com.mentality.fabricwebmap.storage;
import java.nio.file.Path;
/** Keeps configured tile paths contained in either the active world or server root. */
final class TileRootResolver {
    static Path resolve(Path serverRoot, Path worldRoot, boolean insideWorld, String tilesDirectory) {
        Path base = (insideWorld ? worldRoot : serverRoot).toAbsolutePath().normalize();
        Path result = base.resolve(tilesDirectory).normalize();
        if (!result.startsWith(base)) throw new IllegalArgumentException("tilesDirectory escapes its storage root");
        return result;
    }
}
