package jp.ac.tsukuba.eclab.koudounext.core.engine.executor;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.*;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;

import java.util.Timer;
import java.util.TimerTask;


public class SimulationMainLoop {
    private volatile SimulationState mState;
    private SimulationExecutor mExecutor;
    private SimulationConfig mConfig;
    private volatile Timer mTimer;

    public SimulationMainLoop(SimulationConfig config) {
        mState = SimulationState.STOPPED;
        mExecutor = new SimulationExecutor(config);
        mConfig = config;
    }

    public synchronized void start() {
        if (mState == SimulationState.RUNNING) {
            throw new SimulationAlreadyStartedException();
        }
        mState = SimulationState.RUNNING;
        mTimer = new Timer();
        mTimer.schedule(new StepTask(),mConfig.getStepIntervalMillisecond(), mConfig.getStepIntervalMillisecond());
    }

    public synchronized void doStepOnce() {
        if (!mState.equals(SimulationState.PAUSED)) {
            throw new SimulationNotPausedException();
        }

        mExecutor.step();
    }

    public synchronized void pause() {
        if (mState.equals(SimulationState.PAUSED)) {
            throw new SimulationAlreadyPausedException();
        }
        if (mTimer == null) {
            throw new SimulationAlreadyOverException();
        }
        mState = SimulationState.PAUSED;
        mTimer.cancel();
        mTimer = null;
    }

    public synchronized void doPreviousStep() {
        if (!mState.equals(SimulationState.PAUSED)) {
            throw new SimulationNotPausedException();
        }
        mExecutor.prevStep();
    }

    public synchronized void doPreviousSteps(int steps) {
        if (!mState.equals(SimulationState.PAUSED)) {
            throw new SimulationNotPausedException();
        }
        mExecutor.prevSteps(steps);
    }

    public synchronized void resume() {
        if (mState.equals(SimulationState.RUNNING)) {
            throw new SimulationAlreadyResumedException();
        }
        mState = SimulationState.RUNNING;
        mTimer = new Timer();
        mTimer.schedule(new StepTask(),mConfig.getStepIntervalMillisecond(), mConfig.getStepIntervalMillisecond());
    }

    public synchronized void stop() {
        if (!mState.equals(SimulationState.RUNNING)||!mState.equals(SimulationState.PAUSED)) {
            throw new SimulationNotRunningException();
        }
        if (mTimer == null) {
            throw new SimulationAlreadyOverException();
        }
        mState = SimulationState.STOPPED;
        mTimer.cancel();
        mTimer = null;
    }

    public synchronized void forceStop() {
        mState = SimulationState.STOPPED;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public boolean isRunning() {
        return mState.equals(SimulationState.RUNNING);
    }

    public SimulationState getState() {
        return mState;
    }

    class StepTask extends TimerTask {
        @Override
        public void run() {
            try {
                mExecutor.step();
            }catch (SimulationOverMaxStepException e){
                mState = SimulationState.STOPPED;
                forceStop();
            }
        }
    }
}