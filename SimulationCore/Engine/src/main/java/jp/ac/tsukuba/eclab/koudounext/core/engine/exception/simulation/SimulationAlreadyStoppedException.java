package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationAlreadyStoppedException extends BaseException {
    public SimulationAlreadyStoppedException() {
        super("Simulation is already stopped.");
    }
}
