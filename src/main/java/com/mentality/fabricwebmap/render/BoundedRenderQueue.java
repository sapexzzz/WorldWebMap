package com.mentality.fabricwebmap.render;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * Bounded pending queue that keeps a key protected until rendering completes.
 * All operations are O(1) and synchronized because enqueue calls can originate
 * from command and event paths while completion happens on worker threads.
 */
final class BoundedRenderQueue<T> {
    private final int capacity;
    private final ArrayDeque<T>[] pending;
    private final Set<T> protectedKeys = new HashSet<>();
    private final ToIntFunction<T> priority;
    private int pendingCount;

    BoundedRenderQueue(int capacity) {
        this(capacity, ignored -> 0);
    }

    @SuppressWarnings("unchecked")
    BoundedRenderQueue(int capacity, ToIntFunction<T> priority) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
        this.priority = priority;
        this.pending = new ArrayDeque[] { new ArrayDeque<>(), new ArrayDeque<>(), new ArrayDeque<>() };
    }

    synchronized boolean offer(T value) {
        if (protectedKeys.contains(value) || pendingCount >= capacity) return false;
        pending[rank(value)].addLast(value);
        protectedKeys.add(value);
        pendingCount++;
        return true;
    }

    synchronized T pollForRender() {
        for (ArrayDeque<T> jobs : pending) {
            T value = jobs.pollFirst();
            if (value != null) { pendingCount--; return value; }
        }
        return null;
    }

    synchronized void complete(T value) {
        protectedKeys.remove(value);
    }

    synchronized void clearPending() {
        for (ArrayDeque<T> jobs : pending) {
            for (T value : jobs) protectedKeys.remove(value);
            jobs.clear();
        }
        pendingCount = 0;
    }

    synchronized int pendingSize() { return pendingCount; }
    synchronized boolean isProtected(T value) { return protectedKeys.contains(value); }

    private int rank(T value) { return Math.max(0, Math.min(2, priority.applyAsInt(value))); }
}
