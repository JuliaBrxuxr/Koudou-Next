package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationAlreadyResumedException extends BaseException {
    public SimulationAlreadyResumedException() {
        super("Simulation is already resumed.");
    }
}
