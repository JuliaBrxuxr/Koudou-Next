package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationAlreadyOverException extends BaseException {
    public SimulationAlreadyOverException() {
        super("Simulation is already over.");
    }
}
