package jp.ac.tsukuba.eclab.koudounext.core.engine.utils;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.SimulationOverMaxStepException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.SimulationUnableGoPreviousOverFirstStepException;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StepCounter implements Serializable {
    private final int MAX_STEP_COUNT;
    private final AtomicInteger mSteps = new AtomicInteger(0);

    public long getFinalUpdateTimestamp() {
        return mFinalUpdateTimestamp;
    }

    private long mFinalUpdateTimestamp;

    public StepCounter(int maxStepCount) {
        MAX_STEP_COUNT = maxStepCount;
    }

    public void increaseStepCount() {
        if (mSteps.get() > MAX_STEP_COUNT - 1) {
            throw new SimulationOverMaxStepException();
        }
        mSteps.incrementAndGet();
        updateTimestamp();
    }

    public void decreaseStepCount() {
        if (mSteps.get() < 1) {
            throw new SimulationUnableGoPreviousOverFirstStepException();
        }
        mSteps.decrementAndGet();
        updateTimestamp();
    }

    public void decreaseStepCount(int offset){
        if (mSteps.get() - offset < 0) {
            throw new SimulationUnableGoPreviousOverFirstStepException();
        }
        mSteps.set(mSteps.get() - offset);
        updateTimestamp();
    }

    public int getStepCount() {
        return mSteps.get();
    }

    public void reset() {
        mSteps.set(0);
    }

    public boolean isMaxStepCount() {
        return mSteps.get() == MAX_STEP_COUNT;
    }

    private void updateTimestamp(){
        mFinalUpdateTimestamp = System.currentTimeMillis();
    }
}
