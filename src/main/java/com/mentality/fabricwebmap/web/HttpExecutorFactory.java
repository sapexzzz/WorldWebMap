package com.mentality.fabricwebmap.web;
import java.util.concurrent.*;
final class HttpExecutorFactory {
    static final int WORKERS=4, BACKLOG=32;
    static ThreadPoolExecutor create(){ return new ThreadPoolExecutor(WORKERS,WORKERS,0L,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(BACKLOG),new ThreadPoolExecutor.AbortPolicy()); }
    private HttpExecutorFactory(){}
}
