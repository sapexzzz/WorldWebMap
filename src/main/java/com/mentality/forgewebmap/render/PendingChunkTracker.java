package com.mentality.forgewebmap.render;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
/** State-gated storage for chunk-triggered render work. */
final class PendingChunkTracker<T> {
    private final ConcurrentHashMap<String,T> entries=new ConcurrentHashMap<>();
    void recordIfRunning(boolean running,String key,Supplier<T> create,Consumer<T> touch){if(!running)return;entries.compute(key,(ignored,existing)->{if(existing==null)return create.get();touch.accept(existing);return existing;});}
    ConcurrentHashMap<String,T> entries(){return entries;} void clear(){entries.clear();} int size(){return entries.size();}
}
