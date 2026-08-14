package com.mentality.forgewebmap.render;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RenderAdmissionTest { @Test void releasesAfterSuccessFailureAndRejection() { RenderAdmission a=new RenderAdmission(1);assertTrue(a.tryAcquire());assertTrue(a.tryAcquire());assertFalse(a.tryAcquire());a.release();assertTrue(a.tryAcquire());a.release();a.release();assertEquals(2,a.available()); } }
