package com.mentality.forgewebmap.render;
final class SnapshotBudget { interface Clock { long nanoTime(); } private final Clock clock;private final long limit;private long started;SnapshotBudget(Clock clock,long limit){this.clock=clock;this.limit=limit;}void beginTick(){started=clock.nanoTime();}boolean exhausted(){return clock.nanoTime()-started>=limit;} }
