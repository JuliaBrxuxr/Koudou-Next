package jp.ac.tsukuba.eclab.koudounext.core.engine.manager.status;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent.AgentObject;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.StepCounter;

import java.io.Serializable;
import java.util.List;

public class StatusBean implements Serializable {
    private StepCounter mStepCounter;
    private List<AgentObject> mAgents;


    public List<AgentObject> getAgents() {
        return mAgents;
    }

    public void setAgents(List<AgentObject> agents) {
        mAgents = agents;
    }

    public StepCounter getStepCounter() {
        return mStepCounter;
    }

    public void setStepCounter(StepCounter mStepCounter) {
        this.mStepCounter = mStepCounter;
    }
}
