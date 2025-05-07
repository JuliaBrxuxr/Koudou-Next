package jp.ac.tsukuba.eclab.koudounext.core.engine.utils;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.OverMaxStepException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.UnableGoPreviousOverFirstStepException;

import java.util.concurrent.atomic.AtomicInteger;

public class StepCounter {
    private final int MAX_STEP_COUNT;
    private final AtomicInteger mSteps = new AtomicInteger(0);

    public StepCounter(int maxStepCount) {
        MAX_STEP_COUNT = maxStepCount;
    }

    public int increaseStepCount() {
        if (mSteps.get() > MAX_STEP_COUNT - 1) {
            throw new OverMaxStepException();
        }
        return mSteps.incrementAndGet();
    }

    public void decreaseStepCount() {
        if (mSteps.get() < 1) {
            throw new UnableGoPreviousOverFirstStepException();
        }
        mSteps.decrementAndGet();
    }

    public void decreaseStepCount(int offset){
        if (mSteps.get() - offset < 0) {
            throw new UnableGoPreviousOverFirstStepException();
        }
        mSteps.set(mSteps.get() - offset);
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
}
