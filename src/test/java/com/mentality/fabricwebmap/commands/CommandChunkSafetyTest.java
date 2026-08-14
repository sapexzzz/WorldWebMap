package com.mentality.fabricwebmap.commands;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandChunkSafetyTest {
    private String commands() throws Exception { return Files.readString(Path.of("src/main/java/com/mentality/fabricwebmap/commands/WebMapCommands.java")); }
    private String manager() throws Exception { return Files.readString(Path.of("src/main/java/com/mentality/fabricwebmap/render/TileRenderManager.java")); }
    @Test void renderDoesNotGenerateUnloadedChunk() throws Exception {
        assertTrue(commands().contains("RenderJob.Priority.HIGH, true, false"));
    }
    @Test void renderAreaDoesNotGenerateUnloadedChunks() throws Exception {
        assertTrue(commands().contains("RenderJob.Priority.NORMAL, true, false"));
    }
    @Test void fullRenderDoesNotGenerateUnloadedChunks() throws Exception {
        assertFalse(commands().contains("Priority.LOW, true, true"));
    }
    @Test void autoRenderDoesNotGenerateUnloadedChunks() throws Exception {
        assertTrue(manager().contains("RenderJob.Priority.LOW, true, false"));
    }
    @Test void unloadedChunkIsSkippedWithoutForcedGeneration() throws Exception {
        assertFalse(manager().contains("true, !tileExists"));
    }
}
