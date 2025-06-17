package jp.ac.tsukuba.eclab.koudounext.cache;

public class CacheConfig {
    public enum CacheEngineTypeEnum {
        REDIS
    }

    private CacheEngineTypeEnum mCacheEngine = null;
    private String mCacheHost = null;
    private int mCachePort = -1;


    public CacheConfig(CacheEngineTypeEnum cacheEngine, String cacheHost, int cachePort) {
        mCacheEngine = cacheEngine;
        mCacheHost = cacheHost;
        mCachePort = cachePort;
    }

    public CacheEngineTypeEnum getCacheEngine() {
        return mCacheEngine;
    }

    public void setCacheEngine(CacheEngineTypeEnum cacheEngine) {
        mCacheEngine = cacheEngine;
    }

    public String getCacheHost() {
        return mCacheHost;
    }

    public void setCacheHost(String cacheHost) {
        mCacheHost = cacheHost;
    }

    public int getCachePort() {
        return mCachePort;
    }

    public void setCachePort(int cachePort) {
        mCachePort = cachePort;
    }
}