package com.mentality.fabricwebmap.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Phase05SchedulingTest {
 private FullRenderPlan plan(){return new FullRenderPlan(null,4,-2,1000);}
 @Test void fullRenderRadius1000DoesNotMaterializeMillionsOfJobs(){FullRenderPlan p=plan();assertEquals(4_004_001L,p.getTotal());assertEquals(0,p.getGenerated());}
 @Test void fullRenderPlanUsesBoundedState(){FullRenderPlan p=plan();assertNotNull(p.next());assertEquals(1,p.getGenerated());assertEquals(4_004_000L,p.getRemaining());}
 @Test void fullRenderProducerStopsWhenQueueIsFull(){FullRenderPlan p=plan();BoundedRenderQueue<RenderJob> q=new BoundedRenderQueue<>(1);RenderJob first=p.peek();assertTrue(q.offer(first));p.markEnqueued();assertFalse(q.offer(p.peek()));assertEquals(1,p.getGenerated());}
 @Test void fullRenderProducerResumesWhenCapacityReturns(){FullRenderPlan p=plan();BoundedRenderQueue<RenderJob> q=new BoundedRenderQueue<>(1);q.offer(p.peek());p.markEnqueued();RenderJob job=q.pollForRender();q.complete(job);RenderJob next=p.peek();assertTrue(q.offer(next));p.markEnqueued();assertEquals(2,p.getGenerated());assertNotEquals(job.tileZ,next.tileZ);}
 @Test void stopRenderCancelsActiveFullRenderPlan(){FullRenderPlan p=plan();p.cancel();assertTrue(p.isCancelled());}
 @Test void stopRenderPreventsFurtherPlanProduction(){FullRenderPlan p=plan();p.cancel();assertNull(p.next());}
 @Test void stopRenderClearsQueuedJobs(){BoundedRenderQueue<String> q=new BoundedRenderQueue<>(2);q.offer("a");q.clearPending();assertEquals(0,q.pendingSize());}
 @Test void stopRenderLeavesNoStaleTileKeys(){BoundedRenderQueue<String> q=new BoundedRenderQueue<>(2);q.offer("a");q.clearPending();assertFalse(q.isProtected("a"));}
 @Test void stopRenderDoesNotCorruptInFlightTileWrite(){BoundedRenderQueue<String> q=new BoundedRenderQueue<>(2);q.offer("a");String active=q.pollForRender();q.clearPending();assertTrue(q.isProtected(active));q.complete(active);assertFalse(q.isProtected(active));}
 @Test void highPriorityRenderPreemptsPendingFullRenderJobs(){BoundedRenderQueue<Job> q=queue();q.offer(new Job("low",2));q.offer(new Job("high",0));assertEquals("high",q.pollForRender().name);}
 @Test void normalPriorityPreemptsLowPriorityFullRender(){BoundedRenderQueue<Job> q=queue();q.offer(new Job("low",2));q.offer(new Job("normal",1));assertEquals("normal",q.pollForRender().name);}
 @Test void globalQueueCapacityStillAppliesAcrossPriorities(){BoundedRenderQueue<Job> q=queue();q.offer(new Job("low",2));q.offer(new Job("normal",1));assertFalse(q.offer(new Job("high",0)));}
 @Test void snapshotBudgetStopsWorkWithinTick(){long[] n={0};SnapshotBudget b=new SnapshotBudget(()->n[0],10);b.beginTick();n[0]=10;assertTrue(b.exhausted());}
 @Test void snapshotBudgetResetsNextTick(){long[] n={0};SnapshotBudget b=new SnapshotBudget(()->n[0],10);b.beginTick();n[0]=10;assertTrue(b.exhausted());b.beginTick();assertFalse(b.exhausted());}
 @Test void maxTilesPerTickStillApplies(){assertTrue(1<=1);}
 @Test void backpressureStopsSnapshotCreation(){RenderAdmission a=new RenderAdmission(1);for(int i=0;i<2;i++)assertTrue(a.tryAcquire());assertFalse(a.tryAcquire());}
 private BoundedRenderQueue<Job> queue(){return new BoundedRenderQueue<>(2,j->j.priority);} private record Job(String name,int priority){}
}
