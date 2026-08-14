package com.mentality.fabricwebmap.render;

/** Deterministic tick-local sampling budget, deliberately independent of wall-clock sleeps. */
final class SnapshotBudget {
    interface Clock { long nanoTime(); }
    private final Clock clock; private final long limitNanos; private long started;
    SnapshotBudget(Clock clock, long limitNanos) { this.clock=clock; this.limitNanos=limitNanos; }
    void beginTick() { started=clock.nanoTime(); }
    boolean exhausted() { return clock.nanoTime()-started >= limitNanos; }
}
