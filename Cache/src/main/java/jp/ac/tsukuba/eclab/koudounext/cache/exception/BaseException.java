package jp.ac.tsukuba.eclab.koudounext.cache.exception;

public class BaseException extends RuntimeException {
    public BaseException(){
        super("Koudou Exception");
    }
    public BaseException(String message) {
        super(message);
    }
}
