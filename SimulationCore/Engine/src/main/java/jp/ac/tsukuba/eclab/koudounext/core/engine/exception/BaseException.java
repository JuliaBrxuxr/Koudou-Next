package jp.ac.tsukuba.eclab.koudounext.core.engine.exception;

public class BaseException extends RuntimeException {
    BaseException(){}
    BaseException(String message) {
        super(message);
    }
}
