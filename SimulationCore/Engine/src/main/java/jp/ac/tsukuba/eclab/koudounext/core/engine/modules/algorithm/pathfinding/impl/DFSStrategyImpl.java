package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.impl;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.IPathfindingStrategy;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Edge;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.HaversineUtil;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.NodeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DFSStrategyImpl implements IPathfindingStrategy {
    public RoadGraph mGraph;
    private final ArrayList<Integer> mObstacleList;
    private ArrayList<Integer> mFoundPath;
    private double mFoundCost;
    private static final int MAX_DFS_DEPTH = 10000;

    public DFSStrategyImpl(RoadGraph graph, List<Coordinate> obstacleListCoords) {
        this.mGraph = graph;
        this.mObstacleList = new ArrayList<>();
        this.mFoundPath = new ArrayList<>();
        this.mFoundCost = Double.POSITIVE_INFINITY;

        if (obstacleListCoords != null && graph != null && graph.mNodes != null) {
            Set<Integer> tempObstacleSet = new HashSet<>();
            for (Coordinate coordinate : obstacleListCoords) {
                if (coordinate == null) continue;
                Integer id = NodeUtil.findNearestNodeIndex(
                        coordinate.getLatitude(),
                        coordinate.getLongitude(),
                        mGraph);
                if (id != null) tempObstacleSet.add(id);
                for (int i = 0; i < mGraph.mNumNodes; i++) {
                    Node currentNode = mGraph.mNodes.get(i);
                    if (currentNode == null || currentNode.getCoordinate() == null) continue;
                    double dist = HaversineUtil.calculateDistance(
                            coordinate.getLatitude(), coordinate.getLongitude(),
                            currentNode.getCoordinate().getLatitude(), currentNode.getCoordinate().getLongitude());
                    if (dist <= 15) tempObstacleSet.add(i);
                }
            }
            this.mObstacleList.addAll(tempObstacleSet);
        }
    }

    private boolean dfsRecursive(int u_id, int targetNodeId,
                                 ArrayList<Integer> currentPath, boolean[] onPathStack) {
        if (currentPath.size() > MAX_DFS_DEPTH) {
            System.err.println("DFS Error: Path length exceeded MAX_DFS_DEPTH (" + MAX_DFS_DEPTH + ") for agent. Aborting DFS for this path.");
            return false;
        }

        currentPath.add(u_id);
        onPathStack[u_id] = true;

        if (u_id == targetNodeId) {
            // onPathStack[u_id] = false;
            return true;
        }
        if (mGraph.mOutgoingEdges != null && u_id < mGraph.mOutgoingEdges.size() && mGraph.mOutgoingEdges.get(u_id) != null) {
            List<Edge> neighbors = mGraph.mOutgoingEdges.get(u_id);
            for (Edge edge : neighbors) {
                int v_id = edge.getHeadNode();
                if (v_id >= 0 && v_id < mGraph.mNumNodes &&
                        !mObstacleList.contains(v_id) &&
                        !onPathStack[v_id]) {

                    if (dfsRecursive(v_id, targetNodeId, currentPath, onPathStack)) {
                        // onPathStack[u_id] = false;
                        return true;
                    }
                }
            }
        }

        currentPath.remove(currentPath.size() - 1);
        onPathStack[u_id] = false;
        return false;
    }

    @Override
    public double computeShortestPathCost(int sourceNodeId, int targetNodeId) {
        mFoundPath.clear();
        mFoundCost = Double.POSITIVE_INFINITY;
        if (mGraph == null || mGraph.mNodes == null || mGraph.mOutgoingEdges == null ||
                sourceNodeId < 0 || sourceNodeId >= mGraph.mNumNodes ||
                targetNodeId < 0 || targetNodeId >= mGraph.mNumNodes ||
                mGraph.mNodes.get(sourceNodeId) == null || mGraph.mNodes.get(targetNodeId) == null) {
            System.err.println("DFS Error: Invalid graph, source/target node ID, or nodes list not initialized properly.");
            return Double.POSITIVE_INFINITY;
        }
        if (mObstacleList.contains(sourceNodeId) || mObstacleList.contains(targetNodeId)) {
            System.err.println("DFS Error: Source or target node is an obstacle.");
            return Double.POSITIVE_INFINITY;
        }

        ArrayList<Integer> currentPath = new ArrayList<>();
        boolean[] onPathStack = new boolean[mGraph.mNumNodes];

        if (dfsRecursive(sourceNodeId, targetNodeId, currentPath, onPathStack)) {
            this.mFoundPath = new ArrayList<>(currentPath);
            this.mFoundCost = calculatePathTravelTime(this.mFoundPath);
            return this.mFoundCost;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    @Override
    public ArrayList<Integer> getShortestPath(int source, int target) {
        if (mFoundCost == Double.POSITIVE_INFINITY || mFoundPath == null || mFoundPath.isEmpty()) {
            return null;
        }
        return new ArrayList<>(mFoundPath);
    }

    private double calculatePathTravelTime(List<Integer> path) {
        if (path == null || path.size() < 2) {
            return 0;
        }
        double totalTravelTime = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            boolean edgeFound = false;
            if (u >= 0 && u < mGraph.mOutgoingEdges.size() && mGraph.mOutgoingEdges.get(u) != null) {
                for (Edge edge : mGraph.mOutgoingEdges.get(u)) {
                    if (edge.getHeadNode() == v) {
                        totalTravelTime += edge.getTravelTime();
                        edgeFound = true;
                        break;
                    }
                }
            }
            if (!edgeFound) {
                System.err.println("DFS Path Cost Error: Edge not found in path from " + u + " to " + v + ". Path: " + path);
                return Double.POSITIVE_INFINITY;
            }
        }
        return totalTravelTime;
    }
}