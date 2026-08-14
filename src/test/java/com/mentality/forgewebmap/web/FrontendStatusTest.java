package com.mentality.forgewebmap.web;
import org.junit.jupiter.api.Test;import java.nio.file.*;import static org.junit.jupiter.api.Assertions.*;
class FrontendStatusTest{@Test void frontendUsesServerRunningFlag()throws Exception{String js=Files.readString(Path.of("src/main/resources/web/app.js"));assertTrue(js.contains("data.serverRunning")&&js.contains("setStatus(false)"));}}
