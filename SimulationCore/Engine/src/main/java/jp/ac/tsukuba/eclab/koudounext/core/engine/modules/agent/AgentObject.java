package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.agent;

import jp.ac.tsukuba.eclab.koudounext.core.engine.manager.status.StatusManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.ModuleManager;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.IPathfindingStrategy;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.impl.*;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.NodeUtil;

import java.io.Serializable;
import java.util.*;

public class AgentObject implements Serializable {
    private final AgentType AGENT_TYPE;
    private final String UUID;
    private Map<String, Object> mAttributes = new HashMap<>();
    private Queue<AgentActivity> mActivities = new LinkedList<>();
    private Queue<AgentAction> mActions = new LinkedList<>();
    private AgentCondition mCondition;

    public AgentObject(AgentType type) {
        AGENT_TYPE = type;
        UUID = java.util.UUID.randomUUID().toString();

        //TODO: this is just for testing, delete them
        if (AGENT_TYPE.getAgentName().equals("Human")) {
            for (int i = 0; i < 100; i++) {
//                double minLatitude = 36.0737;
//                double maxLatitude = 36.1430;
//                double minLongitude = 140.0845;
//                double maxLongitude = 140.1233;

                double minLatitude = 36.0750;
                double maxLatitude = 36.1200;
                double minLongitude = 140.0900;
                double maxLongitude = 140.1200;

                double latitude = minLatitude + (maxLatitude - minLatitude) * Math.random();
                double longitude = minLongitude + (maxLongitude - minLongitude) * Math.random();

                mActivities.add(new ActivityMovePoint(latitude, longitude));
            }
        }
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
        if (mActions.isEmpty()) {
            AgentActivity activity = mActivities.poll();
            if (activity != null) {
                if ("move_to_point".equals(activity.getAttribute("movement"))) {

                    try {
                        double goalLatitude = (double) activity.getAttribute("latitude");
                        double goalLongitude = (double) activity.getAttribute("longitude");
                        double currentLatitude = (double) getAttribute("latitude");
                        double currentLongitude = (double) getAttribute("longitude");
                        double speed = (double) getAttribute("speed");


                        String pathfindingStrategyName = (String) activity.getAttribute("pathfinding");
                        IPathfindingStrategy pathfindingStrategy;
                        switch (pathfindingStrategyName) {
                            case "DIJKSTRA" -> pathfindingStrategy = new DijkstraStrategyImpl(
                                    ModuleManager.getInstance().getMapManager().getRoadGraph(), new ArrayList<>()
                            );
//                            case "IDA_STAR" -> pathfindingStrategy = new IDAStarStrategyImpl(
//                                    ModuleManager.getInstance().getMapManager().getRoadGraph(), new ArrayList<>()
//                            );
//                            case "DFS" -> pathfindingStrategy = new DFSStrategyImpl(
//                                    ModuleManager.getInstance().getMapManager().getRoadGraph(), new ArrayList<>()
//                            );
                            case "BFS" -> pathfindingStrategy = new BFSStrategyImpl(
                                    ModuleManager.getInstance().getMapManager().getRoadGraph(), new ArrayList<>()
                            );
                            default -> pathfindingStrategy = new AStarStrategyImpl(
                                    ModuleManager.getInstance().getMapManager().getRoadGraph(), new ArrayList<>()
                            );
                        }

                        ArrayList<Integer> calculatedPath = new ArrayList<>();
                        PriorityQueue<NodeUtil.NearNode> currentNodes = NodeUtil.findNearNodes(
                                currentLatitude,
                                currentLongitude,
                                ModuleManager.getInstance().getMapManager().getRoadGraph());
                        PriorityQueue<NodeUtil.NearNode> goalNodes = NodeUtil.findNearNodes(
                                goalLatitude,
                                goalLongitude,
                                ModuleManager.getInstance().getMapManager().getRoadGraph());
                        for (int i = 0; i < 5; i++) {
                            NodeUtil.NearNode currentNode = currentNodes.poll();
                            NodeUtil.NearNode goalNode = goalNodes.poll();

                            if (goalNode == null || currentNode == null) {
                                break;
                            }
                            pathfindingStrategy.computeShortestPathCost(currentNode.getId(), goalNode.getId());
                            calculatedPath = pathfindingStrategy.getShortestPath(
                                    currentNode.getId(), goalNode.getId()
                            );
                            if (calculatedPath != null && !calculatedPath.isEmpty()) {
                                break;
                            }
                        }

                        this.addAttribute("calculatedPath", calculatedPath);


                        //TODO: slot
                        if (calculatedPath == null || calculatedPath.isEmpty()) {
                            return;
                        }

                        RoadGraph graph = ModuleManager.getInstance().getMapManager().getRoadGraph();

                        if (calculatedPath.size() == 1) {
                            Node finalNode = graph.getNodeByIndex(calculatedPath.get(0));
                            if (finalNode != null && finalNode.getCoordinate() != null) {
                                AgentAction arrivalAction = new AgentAction();
                                arrivalAction.addAttribute("action_type", "ACTIVITY_DESTINATION_REACHED");
                                arrivalAction.addAttribute("target_latitude", finalNode.getCoordinate().getLatitude());
                                arrivalAction.addAttribute("target_longitude", finalNode.getCoordinate().getLongitude());
                                mActions.add(arrivalAction);
                            }
                            return;
                        }
                        double distancePerStep = speed * 5;
                        for (int i = 0; i < calculatedPath.size() - 1; i++) {
                            int nodeA_idx = calculatedPath.get(i);
                            int nodeB_idx = calculatedPath.get(i + 1);
                            Node nodeA = graph.getNodeByIndex(nodeA_idx);
                            Node nodeB = graph.getNodeByIndex(nodeB_idx);
                            if (nodeA == null || nodeB == null || nodeA.getCoordinate() == null || nodeB.getCoordinate() == null) {
                                System.err.println("Agent " + getUUID() + ": Path segment " + nodeA_idx + "->" + nodeB_idx +
                                        " contains null node or coordinates. Skipping this segment.");
                                continue;
                            }

                            jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate coordA = nodeA.getCoordinate();
                            jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate coordB = nodeB.getCoordinate();
                            double segmentLength = jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.HaversineUtil.calculateDistance(
                                    coordA.getLatitude(), coordA.getLongitude(),
                                    coordB.getLatitude(), coordB.getLongitude()
                            );
                            if (segmentLength < 1e-6) {
                                if (i == calculatedPath.size() - 2) {
                                    AgentAction finalTargetAction = new AgentAction();
                                    finalTargetAction.addAttribute("action_type", "MOVE_STEP_FINAL_NODE");
                                    finalTargetAction.addAttribute("target_latitude", coordB.getLatitude());
                                    finalTargetAction.addAttribute("target_longitude", coordB.getLongitude());
                                    mActions.add(finalTargetAction);
                                }
                                continue;
                            }

                            double distanceCoveredOnSegment = 0.0;
                            while (distanceCoveredOnSegment < segmentLength) {
                                double remainingOnSegment = segmentLength - distanceCoveredOnSegment;
                                AgentAction stepAction = new AgentAction();

                                if (distancePerStep >= remainingOnSegment - 1e-6) {
                                    stepAction.addAttribute("action_type", (i == calculatedPath.size() - 2) ? "MOVE_STEP_FINAL_NODE" : "MOVE_STEP_TO_NODE");
                                    stepAction.addAttribute("target_latitude", coordB.getLatitude());
                                    stepAction.addAttribute("target_longitude", coordB.getLongitude());
                                    mActions.add(stepAction);
                                    distanceCoveredOnSegment = segmentLength;
                                    break;
                                } else {
                                    distanceCoveredOnSegment += distancePerStep;
                                    double fraction = distanceCoveredOnSegment / segmentLength;
                                    double interpLon = coordA.getLongitude() + fraction * (coordB.getLongitude() - coordA.getLongitude());
                                    double interpLat = coordA.getLatitude() + fraction * (coordB.getLatitude() - coordA.getLatitude());
                                    stepAction.addAttribute("action_type", "MOVE_STEP_INTERMEDIATE");
                                    stepAction.addAttribute("target_latitude", interpLat);
                                    stepAction.addAttribute("target_longitude", interpLon);
                                    mActions.add(stepAction);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (mActions.isEmpty()) {
            return;
        }
        AgentAction currentActionToExecute = mActions.poll();
        if (currentActionToExecute == null) {
            return;
        }
        String actionType = (String) currentActionToExecute.getAttribute("action_type");

        if (actionType != null && actionType.startsWith("MOVE_STEP")) {
            Double targetLat = (Double) currentActionToExecute.getAttribute("target_latitude");
            Double targetLon = (Double) currentActionToExecute.getAttribute("target_longitude");

            if (targetLat != null && targetLon != null) {
                this.updateAttribute("latitude", targetLat);
                this.updateAttribute("longitude", targetLon);
                // System.out.println("Agent " + getUUID() + " moved to: Lat=" + targetLat + ", Lon=" + targetLon + " (Action: " + actionType + ")");
                if (actionType.equals("MOVE_STEP_FINAL_NODE") || actionType.equals("ACTIVITY_DESTINATION_REACHED")) {
//                    System.out.println("Agent " + getUUID() + " has reached the destination of the current move activity.");
                }
            }
        } else if ("ACTIVITY_DESTINATION_REACHED".equals(actionType)) {
//            System.out.println("Agent " + getUUID() + " confirmed at destination for activity.");
        }
    }


}
