package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.impl;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.IPathfindingStrategy;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Edge;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.HaversineUtil;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.NodeUtil;

import java.util.*;

public class IDAStarStrategyImpl implements IPathfindingStrategy {
    public RoadGraph mGraph;
    private final ArrayList<Integer> mObstacleList;
    private ArrayList<Integer> mFoundPath;
    private double mFoundCost;

    private static final double PATH_FOUND_SENTINEL = -1.0;
    private static final double NO_PATH_WITHIN_THRESHOLD_SENTINEL = Double.POSITIVE_INFINITY;
    private static final int MAX_IDA_OUTER_ITERATIONS = 500;
    private static final int MAX_IDA_SEARCH_DEPTH = 10000;


    public IDAStarStrategyImpl(RoadGraph graph, List<Coordinate> obstacleCoordinates) {
        this.mGraph = graph;
        this.mObstacleList = new ArrayList<>();
        this.mFoundPath = new ArrayList<>();
        this.mFoundCost = Double.POSITIVE_INFINITY;

        if (obstacleCoordinates != null && graph != null && graph.mNodes != null) {
            Set<Integer> tempObstacleSet = new HashSet<>(); // Use Set to avoid duplicates
            for (Coordinate coordinate : obstacleCoordinates) {
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

    private double heuristic(int nodeId, int targetNodeId) {
        if (nodeId < 0 || nodeId >= mGraph.mNumNodes || targetNodeId < 0 || targetNodeId >= mGraph.mNumNodes ||
                mGraph.mNodes.get(nodeId) == null || mGraph.mNodes.get(targetNodeId) == null ||
                mGraph.mNodes.get(nodeId).getCoordinate() == null || mGraph.mNodes.get(targetNodeId).getCoordinate() == null) {
            return Double.POSITIVE_INFINITY;
        }
        return HaversineUtil.calculateDistance(
                mGraph.mNodes.get(nodeId).getCoordinate().getLatitude(),
                mGraph.mNodes.get(nodeId).getCoordinate().getLongitude(),
                mGraph.mNodes.get(targetNodeId).getCoordinate().getLatitude(),
                mGraph.mNodes.get(targetNodeId).getCoordinate().getLongitude());
    }

    private double search(int currentNodeId, double gCost, int targetNodeId, double threshold,
                          ArrayList<Integer> currentPath, boolean[] onPathStack) {

        if (currentPath.size() > MAX_IDA_SEARCH_DEPTH) {
            System.err.println("IDA* Search Error: Path length exceeded MAX_IDA_SEARCH_DEPTH (" + MAX_IDA_SEARCH_DEPTH + "). Aborting this search branch.");
            return Double.POSITIVE_INFINITY;
        }

        currentPath.add(currentNodeId);
        onPathStack[currentNodeId] = true;

        double hCost = heuristic(currentNodeId, targetNodeId);
        double fCost = gCost + hCost;

        if (fCost > threshold) {
            currentPath.remove(currentPath.size() - 1);
            onPathStack[currentNodeId] = false;
            return fCost;
        }

        if (currentNodeId == targetNodeId) {
            // onPathStack[currentNodeId] = false;
            return PATH_FOUND_SENTINEL;
        }

        double minNextThreshold = Double.POSITIVE_INFINITY;

        if (mGraph.mOutgoingEdges != null && currentNodeId < mGraph.mOutgoingEdges.size() &&
                mGraph.mOutgoingEdges.get(currentNodeId) != null) {
            List<Edge> neighbors = mGraph.mOutgoingEdges.get(currentNodeId);
            for (Edge edge : neighbors) {
                int neighborNodeId = edge.getHeadNode();

                if (neighborNodeId >=0 && neighborNodeId < mGraph.mNumNodes &&
                        !mObstacleList.contains(neighborNodeId) &&
                        !onPathStack[neighborNodeId]) {

                    double recursiveResult = search(neighborNodeId, gCost + edge.getTravelTime(), targetNodeId, threshold, currentPath, onPathStack);

                    if (recursiveResult == PATH_FOUND_SENTINEL) {
                        // onPathStack[currentNodeId] = false;
                        return PATH_FOUND_SENTINEL;
                    }
                    if (recursiveResult < minNextThreshold) {
                        minNextThreshold = recursiveResult;
                    }
                }
            }
        }

        currentPath.remove(currentPath.size() - 1);
        onPathStack[currentNodeId] = false;
        return minNextThreshold;
    }

    @Override
    public double computeShortestPathCost(int sourceNodeId, int targetNodeId) {
        this.mFoundPath.clear();
        this.mFoundCost = Double.POSITIVE_INFINITY;

        if (mGraph == null || mGraph.mNodes == null || mGraph.mOutgoingEdges == null ||
                sourceNodeId < 0 || sourceNodeId >= mGraph.mNumNodes ||
                targetNodeId < 0 || targetNodeId >= mGraph.mNumNodes ||
                mGraph.mNodes.get(sourceNodeId) == null || mGraph.mNodes.get(targetNodeId) == null) {
            System.err.println("IDA* Error: Invalid graph or source/target node ID.");
            return Double.POSITIVE_INFINITY;
        }
        if (mObstacleList.contains(sourceNodeId) || mObstacleList.contains(targetNodeId)) {
            System.err.println("IDA* Error: Source or target node is an obstacle.");
            return Double.POSITIVE_INFINITY;
        }

        double threshold = heuristic(sourceNodeId, targetNodeId);
        ArrayList<Integer> currentPathForIteration = new ArrayList<>();
        boolean[] onPathStack = new boolean[mGraph.mNumNodes];
        int outerIterations = 0;

        while (threshold < Double.POSITIVE_INFINITY) {
            if (outerIterations++ > MAX_IDA_OUTER_ITERATIONS) {
                System.err.println("IDA* Error: Exceeded MAX_IDA_OUTER_ITERATIONS. Aborting. Threshold was: " + threshold);
                return Double.POSITIVE_INFINITY;
            }

            // System.out.println("IDA* Iteration " + outerIterations + " with threshold: " + threshold);
            currentPathForIteration.clear();
            Arrays.fill(onPathStack, false);

            double searchResult = search(sourceNodeId, 0, targetNodeId, threshold, currentPathForIteration, onPathStack);

            if (searchResult == PATH_FOUND_SENTINEL) {
                this.mFoundPath = new ArrayList<>(currentPathForIteration);
                this.mFoundCost = calculatePathGCost(this.mFoundPath);
                return this.mFoundCost;
            }

            if (searchResult == NO_PATH_WITHIN_THRESHOLD_SENTINEL || searchResult == Double.POSITIVE_INFINITY) {
                // No path found, and no f-value exceeded the current threshold to inform a new one (or new threshold is infinity)
                return Double.POSITIVE_INFINITY;
            }

            if (searchResult <= threshold && threshold != Double.POSITIVE_INFINITY) {
                System.err.println("IDA* Warning: Threshold did not increase. Old: " + threshold + ", New: " + searchResult + ". Breaking to avoid loop.");
                if (Math.abs(searchResult - threshold) < 1e-6) { // If threshold is stuck
                    return Double.POSITIVE_INFINITY;
                }
            }
            threshold = searchResult;
        }
        return Double.POSITIVE_INFINITY;
    }

    private double calculatePathGCost(List<Integer> path) {
        if (path == null || path.size() < 2) {
            return 0;
        }
        double gCost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            boolean edgeFound = false;
            if (u >= 0 && u < mGraph.mOutgoingEdges.size() && mGraph.mOutgoingEdges.get(u) != null) {
                for (Edge edge : mGraph.mOutgoingEdges.get(u)) {
                    if (edge.getHeadNode() == v) {
                        gCost += edge.getTravelTime();
                        edgeFound = true;
                        break;
                    }
                }
            }
            if (!edgeFound) {
                System.err.println("IDA* Path Cost Error: Edge not found in path from " + u + " to " + v + ". Path: " + path);
                return Double.POSITIVE_INFINITY;
            }
        }
        return gCost;
    }

    @Override
    public ArrayList<Integer> getShortestPath(int source, int target) {
        if (mFoundCost == Double.POSITIVE_INFINITY || mFoundPath == null || mFoundPath.isEmpty()) {
            return null;
        }
        return new ArrayList<>(mFoundPath);
    }
}