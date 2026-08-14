package com.mentality.forgewebmap.render;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BoundedRenderQueueTest {
 @Test void forceRenderDoesNotBypassQueueCapacity() { BoundedRenderQueue<String> q=new BoundedRenderQueue<>(2); assertTrue(q.offer("a")); assertTrue(q.offer("b")); assertFalse(q.offer("force")); assertEquals(2,q.pendingSize()); }
 @Test void priorityAndInFlightCompletionWork() { BoundedRenderQueue<Job> q=new BoundedRenderQueue<>(3,j->j.priority); Job low=new Job("l",2), normal=new Job("n",1), high=new Job("h",0); assertTrue(q.offer(low));assertTrue(q.offer(normal));assertTrue(q.offer(high)); assertSame(high,q.pollForRender());assertFalse(q.offer(high));q.complete(high);assertTrue(q.offer(high));assertSame(high,q.pollForRender());q.complete(high);assertSame(normal,q.pollForRender()); }
 record Job(String name,int priority) {}
}
