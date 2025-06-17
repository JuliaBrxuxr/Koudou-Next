package jp.ac.tsukuba.eclab.koudounext.core.engine.executor;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.SimulationUnableGoPreviousOverFirstStepException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.manager.status.StatusManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.ModuleManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent.AgentObject;
import jp.ac.tsukuba.eclab.koudounext.core.engine.test.AgentsUI;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.StepCounter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationExecutor {
    private ModuleManager mModuleManager;

    public SimulationExecutor(SimulationConfig config) {
        StatusManager.getInstance().getStatus().setStepCounter(new StepCounter(config.getMaxStep()));
        mModuleManager = ModuleManager.getInstance();
    }

    public void preStep(){

    }

    public void conflictStep(){

    }

    public void postStep(){

    }

    public void step() throws BaseException {
        StatusManager.getInstance().getStatus().getStepCounter().increaseStepCount();

        long start = System.currentTimeMillis();
        mModuleManager.step();
        long end = System.currentTimeMillis();
        // TODO: DELETE HERE AFTER DEVELOPMENT
        System.out.println("Do step, current count = " + StatusManager.getInstance().getStatus().getStepCounter().getStepCount() + ", cost time = " + (end - start) + "ms");


        if (!StatusManager.getInstance().saveStatus()){
            //TODO: DELETE and ADD a new exception processing
            throw new jp.ac.tsukuba.eclab.koudounext.cache.exception.BaseException("");
        }
    }

    public void prevStep() {
        if (StatusManager.getInstance().getStatus().getStepCounter().getStepCount() < 2) {
            throw new SimulationUnableGoPreviousOverFirstStepException();
        }
        if (!StatusManager.getInstance().loadStatus(-1)){
            //TODO: DELETE and ADD a new exception processing
            throw new jp.ac.tsukuba.eclab.koudounext.cache.exception.BaseException("");
        }

        // TODO: This is just for testing, delete them
        List<Point> points = new ArrayList<>();
        for (AgentObject mAgent : StatusManager.getInstance().getStatus().getAgents()) {
            Double x = (Double) mAgent.getAttribute("x_location");
            Double y = (Double) mAgent.getAttribute("y_location");
            points.add(new Point(x.intValue(), y.intValue()));
        }
        AgentsUI.getInstance().setAgents(points);

        // TODO: DELETE HERE AFTER DEVELOPMENT
        System.out.println("Load prev step, current count = "
                + StatusManager.getInstance().getStatus().getStepCounter().getStepCount());
    }

    public void prevSteps(int offset) {
        if (StatusManager.getInstance().getStatus().getStepCounter().getStepCount() + offset < 0) {
            throw new SimulationUnableGoPreviousOverFirstStepException();
        }
        if (!StatusManager.getInstance().loadStatus(offset)){
            //TODO: DELETE and ADD a new exception processing
            throw new jp.ac.tsukuba.eclab.koudounext.cache.exception.BaseException("");
        }

        // TODO: This is just for testing, delete them
        List<Point> points = new ArrayList<>();
        for (AgentObject mAgent : StatusManager.getInstance().getStatus().getAgents()) {
            Double x = (Double) mAgent.getAttribute("x_location");
            Double y = (Double) mAgent.getAttribute("y_location");
            points.add(new Point(x.intValue(), y.intValue()));
        }
        AgentsUI.getInstance().setAgents(points);
        // TODO: DELETE HERE AFTER DEVELOPMENT
        System.out.println("Load prev step, current count = " + StatusManager.getInstance().getStatus().getStepCounter().getStepCount());

    }
}
