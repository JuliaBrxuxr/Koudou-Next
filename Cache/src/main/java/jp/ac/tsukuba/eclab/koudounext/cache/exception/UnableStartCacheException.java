package jp.ac.tsukuba.eclab.koudounext.cache.exception;

public class UnableStartCacheException extends BaseException {
    public UnableStartCacheException() {
        super("Failed to start cache system, check the configuration file. " +
                "Because of failure, system will not record any data during running!");
    }
}
