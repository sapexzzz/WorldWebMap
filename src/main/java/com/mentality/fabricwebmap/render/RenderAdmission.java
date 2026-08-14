package com.mentality.fabricwebmap.render;

import java.util.concurrent.Semaphore;

/** Bounded admission shared by running workers and their executor backlog. */
final class RenderAdmission {
    private final Semaphore permits;

    RenderAdmission(int workerCount) {
        if (workerCount < 1) throw new IllegalArgumentException("workerCount must be positive");
        permits = new Semaphore(workerCount * 2);
    }

    boolean tryAcquire() { return permits.tryAcquire(); }
    void release() { permits.release(); }
    int available() { return permits.availablePermits(); }
}
