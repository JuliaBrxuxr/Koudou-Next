package jp.ac.tsukuba.eclab.koudounext.cache;

import jp.ac.tsukuba.eclab.koudounext.cache.controller.ICacheEngine;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.UnableSerializeObjectException;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.UnableStartCacheException;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.UnableUnserializeObjectException;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.redis.IllegalRedisConfigException;
import jp.ac.tsukuba.eclab.koudounext.cache.redis.RedisEngine;

import java.io.File;

public class CacheManager {
    private static volatile CacheManager instance = null;
    private ICacheEngine mEngine;
    public CacheManager() {

    }

    public void init(CacheConfig config) {
        if (config == null) {
            throw new NullPointerException("config is null");
        }
        if (config.getCacheEngine().equals(CacheConfig.CacheEngineTypeEnum.REDIS)) {
            mEngine = RedisEngine.getInstance();
            try {
                mEngine.init(config);
            }catch (IllegalRedisConfigException e) {
                mEngine = null;
                throw new UnableStartCacheException();
            }
        }else{
            throw new NullPointerException("config is null");
        }

    }

    public void saveString(String key, String value) {
        mEngine.saveString(key, value);
    }

    public String getString(String key) {
        return mEngine.getString(key);
    }

    public void saveObject(String key, Object value) throws UnableSerializeObjectException  {
        if (mEngine == null) {
            return;
        }
        mEngine.saveObject(key, value);
    }

    public Object getObject(String key) throws UnableUnserializeObjectException {
        return mEngine.getObject(key);
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }
}
