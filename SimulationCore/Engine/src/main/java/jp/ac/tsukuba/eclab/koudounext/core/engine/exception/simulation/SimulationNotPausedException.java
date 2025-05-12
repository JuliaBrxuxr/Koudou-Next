package jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;

public class SimulationNotPausedException extends BaseException {
    public SimulationNotPausedException() {
      super("Simulation not paused, please pause the simulation first.");
    }
}
