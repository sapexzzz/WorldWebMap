package com.mentality.forgewebmap.render;
import java.util.concurrent.Semaphore;
final class RenderAdmission {
    private final Semaphore permits;
    RenderAdmission(int workers) { if (workers < 1) throw new IllegalArgumentException("workerCount must be positive"); permits = new Semaphore(workers * 2); }
    boolean tryAcquire() { return permits.tryAcquire(); }
    void release() { permits.release(); }
    int available() { return permits.availablePermits(); }
}
