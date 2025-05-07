package jp.ac.tsukuba.eclab.koudounext.core.engine;

import jp.ac.tsukuba.eclab.koudounext.core.engine.controller.SimulationController;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;

public class Simulation {
    // This class is the entrance of Application.

    public Simulation(){
        //TODO: Init gRPC listener here and start gRPC loop.


        // TODO: DELETE CODE UNDER HERE WHEN DEVELOPMENT OVER!!! THEY ARE JUST FOR TESTING!!!
        SimulationController controller = new SimulationController();
        SimulationConfig config = new SimulationConfig();
        config.setMaxStep(100);
        config.setStepIntervalMillisecond(10);
        controller.init(config);
        controller.startSimulation();
    }

    public static void main(String[] args) {
        new Simulation();
    }
}
