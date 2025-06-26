package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;

import jp.ac.tsukuba.eclab.koudounext.core.engine.manager.status.StatusManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.IModuleManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.ModuleManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.NodeUtil;
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

        //TODO: JUST FOR TEST, REMOVE THEM
        for (int i = 0; i < 500; i++) {
            AgentType agentType = mAgentTypes.get("Human");
            AgentObject agent = new AgentObject(agentType);


//            double minLatitude = 36.0737;
//            double maxLatitude = 36.1430;
//            double minLongitude = 140.0845;
//            double maxLongitude = 140.1233;


            double minLatitude = 36.0750;
            double maxLatitude = 36.1200;
            double minLongitude = 140.0900;
            double maxLongitude = 140.1200;

            double latitude = minLatitude + (maxLatitude - minLatitude) * Math.random();
            double longitude = minLongitude + (maxLongitude - minLongitude) * Math.random();
            Node node = ModuleManager.getInstance().getMapManager().getRoadGraph().getNodeByIndex(
                    NodeUtil.findNearestNodeIndex(
                            latitude,
                            longitude,
                            ModuleManager.getInstance().getMapManager().getRoadGraph()));

            if (node != null) {
                agent.addAttribute("latitude", node.getCoordinate().getLatitude());
                agent.addAttribute("longitude", node.getCoordinate().getLongitude());
                agent.addAttribute("speed", (double)(0.5 + Math.random() / 2));
//                agent.addAttribute("speed", (double) (0.5 + Math.random() * 5));
                StatusManager.getInstance().getStatus().getAgents().add(agent);
            }
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

        //TODO:DELETE, JUST FOR TESTING
        List<Coordinate> coordinates = new ArrayList<>();
        for (AgentObject agent : StatusManager.getInstance().getStatus().getAgents()) {
            coordinates.add(new Coordinate((double) agent.getAttribute("longitude")
                    , (double) agent.getAttribute("latitude")));
        }
        try {
            AgentsUI.getInstance().updateAllAgentData(StatusManager.getInstance().getStatus().getAgents());
        }catch (Exception e) {

        }
        return true;
    }

    @Override
    public boolean preStep() {
        //TODO:DELETE, JUST FOR TESTING
        List<Coordinate> coordinates = new ArrayList<>();
        for (AgentObject agent : StatusManager.getInstance().getStatus().getAgents()) {
            coordinates.add(new Coordinate((double) agent.getAttribute("longitude")
                    , (double) agent.getAttribute("latitude")));
        }
        try {
            AgentsUI.getInstance().updateAllAgentData(StatusManager.getInstance().getStatus().getAgents());
        }catch (Exception e) {

        }
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
