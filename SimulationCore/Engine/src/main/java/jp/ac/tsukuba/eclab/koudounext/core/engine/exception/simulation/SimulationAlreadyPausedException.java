package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationAlreadyPausedException extends BaseException {
    public SimulationAlreadyPausedException() {
        super("Simulation is already paused.");
    }
}
