package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationOverMaxStepException extends BaseException {
    public SimulationOverMaxStepException() {
        super("Executor over max step.");
    }
}
