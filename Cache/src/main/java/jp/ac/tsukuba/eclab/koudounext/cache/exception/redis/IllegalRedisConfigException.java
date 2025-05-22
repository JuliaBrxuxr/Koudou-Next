package jp.ac.tsukuba.eclab.koudounext.cache.exception.redis;

import jp.ac.tsukuba.eclab.koudounext.cache.exception.BaseException;

public class IllegalRedisConfigException extends BaseException {
    public IllegalRedisConfigException() {
        super("Cannot access redis by configs");
    }
}
