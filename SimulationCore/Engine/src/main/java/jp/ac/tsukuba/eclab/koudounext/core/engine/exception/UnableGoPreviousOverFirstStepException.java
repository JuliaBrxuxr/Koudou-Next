package jp.ac.tsukuba.eclab.koudounext.core.engine.exception;

public class UnableGoPreviousOverFirstStepException extends BaseException {
    public UnableGoPreviousOverFirstStepException() {
        super("Already reached the first step, unable to go previous step over first step.");
    }
}
