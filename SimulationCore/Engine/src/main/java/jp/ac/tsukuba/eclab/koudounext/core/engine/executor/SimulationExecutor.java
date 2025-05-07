package jp.ac.tsukuba.eclab.koudounext.core.engine.executor;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.BaseException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.manager.status.StatusManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.StepCounter;

public class SimulationExecutor {
    private StatusManager mStatusManager;
    private StepCounter mStepCounter;

    public SimulationExecutor(SimulationConfig config) {
        mStatusManager = new StatusManager();
        mStepCounter = new StepCounter(config.getMaxStep());
    }

    public void step() throws BaseException {
        mStepCounter.increaseStepCount();

        // TODO: DELETE HERE AFTER DEVELOPMENT
        System.out.println("Do step, current count = " + mStepCounter.getStepCount());


        mStatusManager.saveStatus();
    }

    public void prevStep() {
        mStatusManager.loadStatus(-1);
        mStepCounter.decreaseStepCount();
    }

    public void prevSteps(int offset) {
        mStepCounter.decreaseStepCount(offset);
        mStatusManager.loadStatus(offset);
    }
}
