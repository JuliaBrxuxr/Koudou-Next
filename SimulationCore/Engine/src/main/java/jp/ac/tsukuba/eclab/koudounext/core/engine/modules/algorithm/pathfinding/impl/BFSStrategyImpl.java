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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class BFSStrategyImpl implements IPathfindingStrategy {
    public RoadGraph mGraph;
    private final ArrayList<Integer> mObstacleList;
    private ArrayList<Integer> mFoundPath;
    private double mFoundCost;

    public BFSStrategyImpl(RoadGraph graph, List<Coordinate> obstacleListCoords) {
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

    @Override
    public double computeShortestPathCost(int sourceNodeId, int targetNodeId) {
        mFoundPath.clear();
        mFoundCost = Double.POSITIVE_INFINITY;
        if (mGraph == null || sourceNodeId < 0 || sourceNodeId >= mGraph.mNumNodes ||
                targetNodeId < 0 || targetNodeId >= mGraph.mNumNodes ||
                mGraph.mNodes.get(sourceNodeId) == null || mGraph.mNodes.get(targetNodeId) == null) {
            System.err.println("BFS Error: Invalid graph or source/target node ID.");
            return Double.POSITIVE_INFINITY;
        }
        if (mObstacleList.contains(sourceNodeId) || mObstacleList.contains(targetNodeId)) {
            System.err.println("BFS Error: Source or target node is an obstacle.");
            return Double.POSITIVE_INFINITY;
        }
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[mGraph.mNumNodes];
        int[] parent = new int[mGraph.mNumNodes];

        for (int i = 0; i < mGraph.mNumNodes; i++) {
            parent[i] = -1;
        }

        queue.add(sourceNodeId);
        visited[sourceNodeId] = true;

        boolean pathExists = false;
        while (!queue.isEmpty()) {
            int u_id = queue.poll();
            if (u_id == targetNodeId) {
                pathExists = true;
                break;
            }
            if (mGraph.mOutgoingEdges.size() > u_id && mGraph.mOutgoingEdges.get(u_id) != null) {
                for (Edge edge : mGraph.mOutgoingEdges.get(u_id)) {
                    int v_id = edge.getHeadNode();
                    if (v_id >= 0 && v_id < mGraph.mNumNodes &&
                            !mObstacleList.contains(v_id) &&
                            !visited[v_id]) {

                        visited[v_id] = true;
                        parent[v_id] = u_id;
                        queue.add(v_id);
                    }
                }
            }
        }
        if (pathExists) {
            int current = targetNodeId;
            while (current != -1) {
                this.mFoundPath.add(current);
                if (current == sourceNodeId) break;
                current = parent[current];
                if (this.mFoundPath.size() > mGraph.mNumNodes) {
                    System.err.println("BFS Path Reconstruction Error: Path too long.");
                    this.mFoundPath.clear();
                    return Double.POSITIVE_INFINITY;
                }
            }
            Collections.reverse(this.mFoundPath);

            if (this.mFoundPath.isEmpty() || this.mFoundPath.get(0) != sourceNodeId) {
                if (sourceNodeId == targetNodeId) {
                    this.mFoundPath.clear();
                    this.mFoundPath.add(sourceNodeId);
                } else {
                    this.mFoundPath.clear();
                    return Double.POSITIVE_INFINITY;
                }
            }
            this.mFoundCost = calculatePathTravelTime(this.mFoundPath);
            return this.mFoundCost;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    @Override
    public ArrayList<Integer> getShortestPath(int source, int target) {
        if (mFoundCost == Double.POSITIVE_INFINITY || mFoundPath.isEmpty()) {
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
                System.err.println("Path Cost Error: Edge not found in path from " + u + " to " + v);
                return Double.POSITIVE_INFINITY;
            }
        }
        return totalTravelTime;
    }
}