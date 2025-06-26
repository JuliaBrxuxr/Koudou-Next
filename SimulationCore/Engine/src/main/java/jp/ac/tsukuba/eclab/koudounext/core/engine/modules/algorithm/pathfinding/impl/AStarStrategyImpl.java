package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.impl;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.IPathfindingStrategy;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.HaversineUtil;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Vertex;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Edge;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.NodeUtil;


import java.util.*;


public class AStarStrategyImpl implements IPathfindingStrategy {
    public RoadGraph mGraph;

    private final int inf = Integer.MAX_VALUE;
    private final PriorityQueue<Vertex> mDistances;
    private final ArrayList<Vertex> mSettled;
    private final ArrayList<Integer> mObstacleList;

    public AStarStrategyImpl(RoadGraph graph, List<Coordinate> obstacleListCoords) {
        mGraph = graph;
        Comparator<Vertex> comparator = new DistanceComparator();
        mDistances = new PriorityQueue<>(comparator);
        mSettled = new ArrayList<>();
        mObstacleList = new ArrayList<>();

        if (obstacleListCoords != null && graph != null && graph.mNodes != null) {
            for (Coordinate coordinate : obstacleListCoords) {
                if (coordinate == null) continue;
                Integer id = NodeUtil.findNearestNodeIndex(
                        coordinate.getLatitude(),
                        coordinate.getLongitude(),
                        mGraph);
                if (id != null && !mObstacleList.contains(id)) {
                    mObstacleList.add(id);
                }
                for (int i = 0; i < mGraph.mNumNodes; i++) {
                    Node currentNode = mGraph.mNodes.get(i);
                    if (currentNode == null || currentNode.getCoordinate() == null) continue;

                    double dist = HaversineUtil.calculateDistance(
                            coordinate.getLatitude(),
                            coordinate.getLongitude(),
                            currentNode.getCoordinate().getLatitude(),
                            currentNode.getCoordinate().getLongitude());
                    if (dist <= 15) {
                        Integer nearbyNodeId = i;
                        if (!mObstacleList.contains(nearbyNodeId)) {
                            mObstacleList.add(nearbyNodeId);
                        }
                    }
                }
            }
        }
    }

    private double calculateHeuristicValue(int nodeId, int targetNodeId) {
        if (targetNodeId == -1 || nodeId == targetNodeId) {
            return 0;
        }
        if (nodeId < 0 || nodeId >= mGraph.mNumNodes ||
                targetNodeId < 0 || targetNodeId >= mGraph.mNumNodes ||
                mGraph.mNodes.get(nodeId) == null || mGraph.mNodes.get(targetNodeId) == null ||
                mGraph.mNodes.get(nodeId).getCoordinate() == null || mGraph.mNodes.get(targetNodeId).getCoordinate() == null) {
            // System.err.println("Warning: Invalid node ID or missing coordinate for heuristic calculation.");
            return Double.POSITIVE_INFINITY;
        }

        return HaversineUtil.calculateDistance(
                mGraph.mNodes.get(nodeId).getCoordinate().getLatitude(),
                mGraph.mNodes.get(nodeId).getCoordinate().getLongitude(),
                mGraph.mNodes.get(targetNodeId).getCoordinate().getLatitude(),
                mGraph.mNodes.get(targetNodeId).getCoordinate().getLongitude()
        );
    }


    @Override
    public double computeShortestPathCost(int sourceNodeId, int targetNodeId) {
        mDistances.clear();
        mSettled.clear();
        for (int i = 0; i < mGraph.mNumNodes; i++) {
            mSettled.add(null);
        }
        double hSource = calculateHeuristicValue(sourceNodeId, targetNodeId);
        mDistances.add(new Vertex(sourceNodeId, 0, sourceNodeId, hSource)); // g=0, parent=self, h=h(source,target)
        while (!mDistances.isEmpty()) {
            Vertex currentBestVertex = mDistances.poll();
            int u_id = currentBestVertex.getId();
            double gCost_u = currentBestVertex.getDistance();
            if (mSettled.get(u_id) != null && mSettled.get(u_id).getDistance() <= gCost_u) {
                continue;
            }
            mSettled.set(u_id, currentBestVertex);
            if (targetNodeId != -1 && u_id == targetNodeId) {
                return gCost_u;
            }
            if (mGraph.mOutgoingEdges.size() <= u_id || mGraph.mOutgoingEdges.get(u_id) == null) {
                continue;
            }
            ArrayList<Edge> edges = mGraph.mOutgoingEdges.get(u_id);
            for (Edge edge : edges) {
                int v_id = edge.getHeadNode();

                if (mObstacleList.contains(v_id) || mSettled.get(v_id) != null) {
                    continue;
                }

                double gCost_v_through_u = gCost_u + edge.getTravelTime();
                double hCost_v = calculateHeuristicValue(v_id, targetNodeId);
                mDistances.add(new Vertex(v_id, gCost_v_through_u, u_id, hCost_v));
            }
        }

        if (targetNodeId != -1) {
            Vertex targetInfo = (targetNodeId < mSettled.size()) ? mSettled.get(targetNodeId) : null;
            return (targetInfo != null) ? targetInfo.getDistance() : inf;
        }
        return 0;
    }

    @Override
    public ArrayList<Integer> getShortestPath(int source, int target) {
        if (target < 0 || target >= mSettled.size() || mSettled.get(target) == null || mSettled.get(target).getDistance() == inf) {
            return null;
        }

        ArrayList<Integer> path = new ArrayList<>();
        int current = target;
        while (mSettled.get(current) != null) {
            path.add(current);
            if (current == source) {
                break;
            }
            int parent = mSettled.get(current).getParent();
            if (parent == current && current != source) {
                System.err.println("A* Path reconstruction error: Node's parent is itself (node " + current + ")");
                return null;
            }
            current = parent;
            if (path.size() > mGraph.mNumNodes) {
                System.err.println("A* Path reconstruction error: Path too long (>" + mGraph.mNumNodes + " nodes).");
                return null;
            }
        }

        if (path.isEmpty() || path.get(path.size() - 1) != source) {
            if (source == target && mSettled.get(source) != null && mSettled.get(source).getDistance() == 0) {
                if (path.isEmpty()) path.add(source);
            } else {
                // System.err.println("A* Path reconstruction error: Path does not end at source node.");
                return null;
            }
        }

        Collections.reverse(path);
        return path;
    }
    static class DistanceComparator implements Comparator<Vertex> {
        public int compare(Vertex vertex1, Vertex vertex2) {
            return Double.compare(
                    vertex1.getDistance() + vertex1.getHeuristic(),
                    vertex2.getDistance() + vertex2.getHeuristic()
            );
        }
    }
}