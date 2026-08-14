package com.mentality.fabricwebmap.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RenderAdmissionTest {
    @Test void admissionIsBoundedAndReleasedOnEveryExitPath() {
        RenderAdmission admission = new RenderAdmission(1);
        assertTrue(admission.tryAcquire()); // running worker
        assertTrue(admission.tryAcquire()); // bounded backlog
        assertFalse(admission.tryAcquire()); // snapshot must not be built
        admission.release(); // success
        assertTrue(admission.tryAcquire());
        admission.release(); // worker failure
        admission.release(); // executor rejection/shutdown
        assertEquals(2, admission.available());
    }
}
