package jp.ac.tsukuba.eclab.koudounext.core.engine.utils;

public class SimulationConfig {
    private int mMaxStep;
    private int mStepIntervalMillisecond;

    public int getMaxStep() {
        return mMaxStep;
    }

    public void setMaxStep(int maxStep) {
        mMaxStep = maxStep;
    }

    public int getStepIntervalMillisecond() {
        return mStepIntervalMillisecond;
    }

    public void setStepIntervalMillisecond(int stepIntervalMillisecond) {
        mStepIntervalMillisecond = stepIntervalMillisecond;
    }
}
