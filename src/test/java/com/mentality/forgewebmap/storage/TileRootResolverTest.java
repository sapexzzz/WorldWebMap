package com.mentality.forgewebmap.storage;
import com.mentality.forgewebmap.config.WebMapConfig;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;import java.nio.file.*;import static org.junit.jupiter.api.Assertions.*;
class TileRootResolverTest{@TempDir Path d;
 @Test void customLevelNameUsesActualWorldPath(){assertEquals(d.resolve("my_survival/tiles").toAbsolutePath(),TileRootResolver.resolve(d,d.resolve("my_survival"),true,"tiles"));}
 @Test void defaultWorldPathRemainsCompatible(){assertEquals(d.resolve("world/tiles").toAbsolutePath(),TileRootResolver.resolve(d,d.resolve("world"),true,"tiles"));}
 @Test void saveTilesInsideWorldFolderFalseUsesServerRoot(){assertEquals(d.resolve("tiles").toAbsolutePath(),TileRootResolver.resolve(d,d.resolve("world"),false,"tiles"));}
 @Test void tileDirectoryTraversalIsRejectedOrContained(){assertThrows(IllegalArgumentException.class,()->TileRootResolver.resolve(d,d.resolve("world"),true,"../bad"));}
 @Test void productionStorageUsesActualWorldPath()throws Exception{TileStorage s=TileStorage.forWorld(config("saveTilesInsideWorldFolder=true\ntilesDirectory=maps/tiles\n"),d.resolve("custom-level"));assertEquals(d.resolve("custom-level/maps/tiles").toAbsolutePath(),s.getTilesRoot());}
 @Test void productionStorageHonorsInsideWorldFlag()throws Exception{TileStorage s=TileStorage.forWorld(config("saveTilesInsideWorldFolder=true\ntilesDirectory=tiles\n"),d.resolve("world"));assertEquals(d.resolve("world/tiles").toAbsolutePath(),s.getTilesRoot());}
 @Test void productionStorageHonorsOutsideWorldFlag()throws Exception{TileStorage s=TileStorage.forWorld(config("saveTilesInsideWorldFolder=false\ntilesDirectory=tiles\n"),d.resolve("world"));assertEquals(d.resolve("tiles").toAbsolutePath(),s.getTilesRoot());}
 private WebMapConfig config(String v)throws Exception{Path c=d.resolve("config");Files.createDirectories(c);Files.writeString(c.resolve("forgewebmap-common.properties"),v);WebMapConfig r=new WebMapConfig(c);r.load();return r;}}
