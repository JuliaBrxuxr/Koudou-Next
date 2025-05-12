package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationNotRunningException extends BaseException {
    public SimulationNotRunningException() {
        super("Simulation not running, please run it first.");
    }
}
