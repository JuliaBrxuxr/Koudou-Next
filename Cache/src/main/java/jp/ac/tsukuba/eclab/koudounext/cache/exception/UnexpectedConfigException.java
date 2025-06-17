package jp.ac.tsukuba.eclab.koudounext.cache.exception;

public class UnexpectedConfigException extends RuntimeException {
    public UnexpectedConfigException() {
        super("Cache configuration is not correct");
    }
}
