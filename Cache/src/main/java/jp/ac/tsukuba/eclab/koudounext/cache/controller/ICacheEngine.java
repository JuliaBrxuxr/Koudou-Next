package jp.ac.tsukuba.eclab.koudounext.cache.controller;

import jp.ac.tsukuba.eclab.koudounext.cache.CacheConfig;

public interface ICacheEngine {
    public void init(CacheConfig config);
    public void saveString(String key, String value);
    public String getString(String key);
    public void saveObject(String key, Object value);
    public Object getObject(String key);
}
