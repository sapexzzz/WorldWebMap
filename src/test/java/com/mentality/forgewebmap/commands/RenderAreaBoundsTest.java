package com.mentality.forgewebmap.commands;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RenderAreaBoundsTest { @Test void safeAtIntegerExtremes() { assertFalse(RenderAreaBounds.isAllowed(Integer.MIN_VALUE,0,Integer.MAX_VALUE,0));assertFalse(RenderAreaBounds.isAllowed(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE));assertFalse(RenderAreaBounds.isAllowed(5,0,4,0));assertTrue(RenderAreaBounds.isAllowed(-50,-50,49,49));assertFalse(RenderAreaBounds.isAllowed(-50,-50,50,49));assertTrue(RenderAreaBounds.isAllowed(-2,-2,2,2)); } }
