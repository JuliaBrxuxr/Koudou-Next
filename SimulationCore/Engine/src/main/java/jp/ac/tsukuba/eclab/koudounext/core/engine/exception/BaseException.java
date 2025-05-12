package jp.ac.tsukuba.eclab.koudounext.core.engine.exception;

public class BaseException extends RuntimeException {
    public BaseException(){
        super("Koudou Exception");
    }
    public BaseException(String message) {
        super(message);
    }
}
