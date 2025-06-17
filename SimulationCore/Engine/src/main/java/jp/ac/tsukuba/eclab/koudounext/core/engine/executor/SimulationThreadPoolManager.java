package jp.ac.tsukuba.eclab.koudounext.core.engine.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulationThreadPoolManager {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService mPool = Executors.newFixedThreadPool(THREAD_COUNT);

    public static ExecutorService getThreadPool() {
        return mPool;
    }

    public static void shutdown() {
        mPool.shutdown();
    }
}
