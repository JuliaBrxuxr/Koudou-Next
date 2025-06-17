package jp.ac.tsukuba.eclab.koudounext.cache.redis;

import jp.ac.tsukuba.eclab.koudounext.cache.CacheConfig;
import jp.ac.tsukuba.eclab.koudounext.cache.controller.ICacheEngine;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.UnableSerializeObjectException;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.UnableUnserializeObjectException;
import jp.ac.tsukuba.eclab.koudounext.cache.exception.redis.IllegalRedisConfigException;
import jp.ac.tsukuba.eclab.koudounext.cache.util.SerializeUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisEngine implements ICacheEngine {
    private static volatile RedisEngine instance = null;
    private JedisPool mPool;

    public RedisEngine() {

    }

    @Override
    public void init(CacheConfig config) {
        if (config == null || config.getCacheHost() == null || config.getCachePort() == -1) {
            throw new IllegalRedisConfigException();
        }
        try {
            mPool = new JedisPool(new JedisPoolConfig(), config.getCacheHost(), config.getCachePort());
            Jedis jedis = mPool.getResource();
            jedis.close();
        } catch (Exception e) {
            throw new IllegalRedisConfigException();
        }

    }

    @Override
    public void saveString(String key, String value) {
        Jedis jedis = mPool.getResource();
        jedis.set(key, value);
        jedis.close();
    }

    @Override
    public String getString(String key) {
        Jedis jedis = mPool.getResource();
        String value = jedis.get(key);
        jedis.close();
        return value;
    }

    @Override
    public Object getObject(String key) throws UnableUnserializeObjectException {
        Jedis jedis = mPool.getResource();
        Object value = SerializeUtil.unserialize(jedis.get(key.getBytes()));
        jedis.close();
        return value;
    }

    @Override
    public void saveObject(String key, Object value) throws UnableSerializeObjectException {
        Jedis jedis = mPool.getResource();
        byte[] bytes = SerializeUtil.serialize(value);
        jedis.set(key.getBytes(), bytes);
        jedis.close();
    }

    public static RedisEngine getInstance() {
        if (instance == null) {
            synchronized (RedisEngine.class) {
                if (instance == null) {
                    instance = new RedisEngine();
                }
            }
        }
        return instance;
    }
}
