package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationAlreadyStartedException extends BaseException {
    public SimulationAlreadyStartedException() {
        super("Simulation is already started");
    }
}
