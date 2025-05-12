package jp.ac.tsukuba.eclab.koudounext.core.engine.controller;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.*;
import jp.ac.tsukuba.eclab.koudounext.core.engine.executor.SimulationMainLoop;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.ModuleManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;

public class SimulationController {
    private static volatile SimulationController instance = null;
    private SimulationMainLoop mLoop = null;
    private ModuleManager mModuleManager;

    public SimulationController() {
        mModuleManager = new ModuleManager();
    }

    public void init(SimulationConfig config) {
        mLoop = new SimulationMainLoop(config);
        mModuleManager.loadAll();
    }

    public void startSimulation() throws SimulationAlreadyStartedException {
        mLoop.start();
    }

    public void stopSimulation() throws SimulationNotRunningException {
         mLoop.stop();
    }

    public void pauseSimulation() throws SimulationAlreadyPausedException {
        mLoop.pause();
    }

    public void resumeSimulation() throws SimulationAlreadyResumedException {
        mLoop.resume();
    }

    public void doSimulationNextStep() throws SimulationNotPausedException {
        mLoop.doStepOnce();
    }

    public void doSimulationPrevStep() throws SimulationNotPausedException {
        mLoop.doPreviousStep();
    }

    public void doSimulationPrevSteps(int steps) throws SimulationNotPausedException {
        mLoop.doPreviousSteps(steps);
    }

    public static SimulationController getInstance() {
        if (instance == null) {
            synchronized (SimulationController.class) {
                if (instance == null) {
                    instance = new SimulationController();
                }
            }
        }
        return instance;
    }
}
