package com.mentality.fabricwebmap.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundedRenderQueueTest {
    @Test
    void capacityAndInFlightProtectionRemainBounded() {
        BoundedRenderQueue<String> queue = new BoundedRenderQueue<>(2);
        assertTrue(queue.offer("first"));
        assertTrue(queue.offer("second"));
        assertFalse(queue.offer("forced-third"));

        assertEquals("first", queue.pollForRender());
        assertTrue(queue.isProtected("first"));
        assertFalse(queue.offer("first"));
        queue.complete("first");
        assertTrue(queue.offer("first"));
    }

    @Test
    void forceRenderDoesNotBypassQueueCapacity() {
        BoundedRenderQueue<String> queue = new BoundedRenderQueue<>(2);
        assertTrue(queue.offer("normal-a"));
        assertTrue(queue.offer("normal-b"));
        assertFalse(queue.offer("force=true-c"));
        assertEquals(2, queue.pendingSize());
    }

    @Test
    void priorityAndCompletionAreDeterministic() {
        BoundedRenderQueue<Job> queue = new BoundedRenderQueue<>(3, job -> job.priority);
        Job low = new Job("low", 2);
        Job normal = new Job("normal", 1);
        Job high = new Job("high", 0);
        assertTrue(queue.offer(low)); assertTrue(queue.offer(normal)); assertTrue(queue.offer(high));
        assertSame(high, queue.pollForRender());
        assertFalse(queue.offer(high));
        queue.complete(high); // same path used after worker failure/rejection/success
        assertTrue(queue.offer(high));
        assertSame(high, queue.pollForRender());
        queue.complete(high);
        assertSame(normal, queue.pollForRender());
    }

    private record Job(String key, int priority) { }
}
