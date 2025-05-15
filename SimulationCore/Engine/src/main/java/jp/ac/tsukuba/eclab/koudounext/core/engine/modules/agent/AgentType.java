package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;

import javax.management.ObjectName;
import java.util.*;

public class AgentType {
    private String mAgentName = "";
    private Map<String,String> mAttributes = new HashMap<>();
    private Map<String,Object> mDefaultValues = new HashMap<>();
    private List<String> mConditions = new ArrayList<String>();

    public String getAgentName() {
        return mAgentName;
    }

    public void setAgentName(String agentName) {
        mAgentName = agentName;
    }

    public Map<String, String> getAttributes() {
        return mAttributes;
    }

    public void addAttribute(String key, String value,Object defaultValue) {
        mAttributes.put(key, value);
        mDefaultValues.put(key, defaultValue);
    }

    public Set<String> attributes() {
        return mAttributes.keySet();
    }

    public String getAttributeType(String attributeName){
        return mAttributes.get(attributeName);
    }

    public Object getAttributeDefaultValue(String attributeName){
        return mDefaultValues.get(attributeName);
    }

    public List<String> getConditions() {
        return mConditions;
    }

    public void setConditions(List<String> conditions) {
        mConditions = conditions;
    }
}
