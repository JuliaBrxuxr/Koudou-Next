package jp.ac.tsukuba.eclab.koudounext.core.engine.controller;

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

    public void startSimulation() {
        mLoop.start();
    }

    public void stopSimulation() {
         mLoop.stop();
    }

    public void pauseSimulation() {
        mLoop.pause();
    }

    public void resumeSimulation() {
        mLoop.resume();
    }

    public void doSimulationNextStep() {
        mLoop.doStepOnce();
    }

    public void doSimulationPrevStep() {
        mLoop.doPreviousStep();
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
