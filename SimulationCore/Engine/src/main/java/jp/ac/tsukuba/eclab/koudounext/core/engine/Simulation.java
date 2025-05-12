package jp.ac.tsukuba.eclab.koudounext.core.engine;

import jp.ac.tsukuba.eclab.koudounext.core.engine.controller.SimulationController;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;

public class Simulation {
    // This class is the entrance of Application.

    public Simulation(){
        //TODO: Init gRPC listener here and start gRPC loop.


        // TODO: DELETE CODE UNDER HERE WHEN DEVELOPMENT OVER!!! THEY ARE JUST FOR TESTING!!!
        SimulationController controller = new SimulationController();
        SimulationConfig config = new SimulationConfig();
        config.setMaxStep(100);
        config.setStepIntervalMillisecond(50);
        controller.init(config);
        new Thread(new Runnable() {
            public void run() {
                try {
                    controller.startSimulation();
                    Thread.sleep(500);
                    controller.pauseSimulation();
                    Thread.sleep(1000);
                    controller.doSimulationPrevStep();
                    Thread.sleep(1000);
                    controller.doSimulationNextStep();
                    Thread.sleep(1000);
                    controller.doSimulationPrevSteps(5);
                    Thread.sleep(1000);
                    controller.resumeSimulation();
                    Thread.sleep(100);
                    controller.pauseSimulation();
                    Thread.sleep(1000);
                    controller.resumeSimulation();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (BaseException be){
                    System.out.println("KOUDOU EXCEPTION: " + be.getMessage());
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        new Simulation();
    }
}
