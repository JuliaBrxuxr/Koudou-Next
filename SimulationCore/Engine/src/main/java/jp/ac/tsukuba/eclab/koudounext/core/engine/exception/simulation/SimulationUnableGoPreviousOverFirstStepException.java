package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationUnableGoPreviousOverFirstStepException extends BaseException {
    public SimulationUnableGoPreviousOverFirstStepException() {
        super("Already reached the first step, unable to go previous step over first step.");
    }
}
