package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;

import java.util.HashMap;
import java.util.Map;

public class Agent {
    private Map<String, Object> mAttributes = new HashMap<>();

    public void setParameters(Map<String, Object> params) {
        mAttributes.putAll(params);
    }
}
