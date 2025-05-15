package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;

import jp.ac.tsukuba.eclab.koudounext.core.engine.test.AgentsUI;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AgentObject {
    private final AgentType AGENT_TYPE;
    private Map<String, Object> mAttributes = new HashMap<>();
    private Queue<AgentActivity> mActivities = new LinkedList<>();
    private AgentCondition mCondition;

    public AgentObject(AgentType type) {
        AGENT_TYPE = type;
    }

    public AgentType getAgentType() {
        return AGENT_TYPE;
    }

    public void addAttribute(String name, Object value) {
        mAttributes.put(name, value);
    }

    public void updateAttribute(String name, Object value) {
        mAttributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return mAttributes.get(name);
    }

    public void step() {

        // TODO: Here needs to an engine of parsing activity

        // TODO: DELETE! There code here are just for testing
        double x = (Math.random() - 0.5) * 400;
        double y = (Math.random() - 0.5) * 400;
        updateAttribute("x_location", x);
        updateAttribute("y_location", y);
    }
}
