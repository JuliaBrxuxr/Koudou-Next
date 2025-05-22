package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;


import java.io.Serializable;
import java.util.*;

public class AgentObject implements Serializable {
    private final AgentType AGENT_TYPE;
    private final String UUID;
    private Map<String, Object> mAttributes = new HashMap<>();
    private Queue<AgentActivity> mActivities = new LinkedList<>();
    private AgentCondition mCondition;

    public AgentObject(AgentType type) {
        AGENT_TYPE = type;
        UUID = java.util.UUID.randomUUID().toString();
    }

    public String getUUID() {
        return UUID;
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
        double r_x = (Math.random() - 0.5) * 25;
        double r_y = (Math.random() - 0.5) * 25;
        double now_x = (double) getAttribute("x_location");
        double now_y = (double) getAttribute("y_location");
        updateAttribute("x_location", now_x + r_x);
        updateAttribute("y_location", now_y + r_y);
    }
}
