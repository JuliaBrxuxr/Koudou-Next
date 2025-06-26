package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AgentActivity implements Serializable {
    private Map<String, Object> mAttributes = new HashMap<>();
    public void addAttribute(String name, Object value) {
        mAttributes.put(name, value);
    }

    public void updateAttribute(String name, Object value) {
        mAttributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return mAttributes.get(name);
    }
}
