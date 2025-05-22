package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;

import jp.ac.tsukuba.eclab.koudounext.core.engine.manager.status.StatusManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.IModuleManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.test.AgentsUI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class AgentManagerImpl implements IModuleManager {
    private Map<String, AgentType> mAgentTypes;
    private final ExecutorService mThreadPool;

    public AgentManagerImpl(ExecutorService threadPool) {
        StatusManager.getInstance().getStatus().setAgents(new ArrayList<>());
        this.mAgentTypes = new HashMap<>();
        mThreadPool = threadPool;
    }

    @Override
    public boolean load() {
        //TODO: loader for agents config

        // TODO: This is just for testing, delete them
        String jsonStr = null;
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("test_agents.json");
        if (input == null) {
            throw new RuntimeException("Resource not found");
        }
        try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            jsonStr = scanner.hasNext() ? scanner.next() : "";
        }
        JSONObject root = new JSONObject(jsonStr);
        JSONArray agents = root.getJSONArray("agents");
        for (int i = 0; i < agents.length(); i++) {
            JSONObject agent = agents.getJSONObject(i);
            AgentType agentType = new AgentType();
            agentType.setAgentName(agent.getString("type"));
            JSONArray attributes = agent.getJSONArray("attributes");
            for (int j = 0; j < attributes.length(); j++) {
                JSONObject attribute = attributes.getJSONObject(j);
                String type = attribute.getString("type");
                switch (type) {
                    case "number": {
                        agentType.addAttribute(attribute.getString("name"),
                                attribute.getString("type"),
                                attribute.getDouble("default_value"));
                        break;
                    }
                    case "text":
                    default:
                        agentType.addAttribute(attribute.getString("name"),
                                attribute.getString("type"),
                                attribute.getString("default_value"));
                        break;
                }
            }
            mAgentTypes.put(agentType.getAgentName(), agentType);
        }
        for (int i = 0; i < 1000; i++) {
            AgentType agentType = mAgentTypes.get("Zombie");
            AgentObject agent = new AgentObject(agentType);
            Map<String, String> attributes = agentType.getAttributes();
            for (String attribute : attributes.keySet()) {
                agent.addAttribute(attribute, (Math.random()+4) * 50);
            }
            StatusManager.getInstance().getStatus().getAgents().add(agent);
        }

        return true;
    }

    @Override
    public boolean step() {
        int taskCount = StatusManager.getInstance().getStatus().getAgents().size();
        CountDownLatch latch = new CountDownLatch(taskCount);
        for (int i = 0; i < taskCount; i++) {
            int taskId = i;
            mThreadPool.submit(() -> {
                try {
                    StatusManager.getInstance().getStatus().getAgents().get(taskId).step();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        // TODO: This is just for testing, delete them
        List<Point> points = new ArrayList<>();
        for (AgentObject mAgent : StatusManager.getInstance().getStatus().getAgents()) {
            Double x = (Double)mAgent.getAttribute("x_location");
            Double y = (Double)mAgent.getAttribute("y_location");
            points.add(new Point(x.intValue(),y.intValue()));
        }
        AgentsUI.getInstance().setAgents(points);
        return true;
    }

    @Override
    public boolean preStep() {
        return false;
    }

    @Override
    public boolean conflictStep() {
        return false;
    }

    @Override
    public boolean postStep() {
        return false;
    }


}
