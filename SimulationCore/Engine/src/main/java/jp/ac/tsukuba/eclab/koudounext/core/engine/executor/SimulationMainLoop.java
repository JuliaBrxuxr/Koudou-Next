package jp.ac.tsukuba.eclab.koudounext.core.engine.executor;

import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.OverMaxStepException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;

import java.util.Timer;
import java.util.TimerTask;


public class SimulationMainLoop {
    private volatile SimulationState mState;
    private SimulationExecutor mExecutor;
    private SimulationConfig mConfig;
    private volatile Timer mTimer = new Timer();

    public SimulationMainLoop(SimulationConfig config) {
        mState = SimulationState.STOPPED;
        mExecutor = new SimulationExecutor(config);
        mConfig = config;
        mTimer = new Timer();
    }

    public synchronized void start() {
        if (mState == SimulationState.RUNNING) {
            return;
        }
        mState = SimulationState.RUNNING;
        mTimer.schedule(new StepTask(),mConfig.getStepIntervalMillisecond(), mConfig.getStepIntervalMillisecond());
    }

    public synchronized void doStepOnce() {
        if (!mState.equals(SimulationState.PAUSED)) {
            // Do step once must in paused status
            return;
        }

        // Make executor do once
    }

    public synchronized void pause() {
        mState = SimulationState.PAUSED;
        mTimer.cancel();
    }

    public synchronized void doPreviousStep() {

    }

    public synchronized void resume() {
        mState = SimulationState.RUNNING;
        mTimer.schedule(new StepTask(),mConfig.getStepIntervalMillisecond(), mConfig.getStepIntervalMillisecond());
    }

    public synchronized void stop() {
        mState = SimulationState.STOPPED;
        mTimer.cancel();
        mTimer = null;
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
            }catch (OverMaxStepException e){
                stop();
            }
        }
    }
}