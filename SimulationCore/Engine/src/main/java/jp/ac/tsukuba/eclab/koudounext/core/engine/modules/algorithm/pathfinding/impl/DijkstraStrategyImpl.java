package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.impl;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.IPathfindingStrategy;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Coordinate;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Edge;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Vertex;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.HaversineUtil;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.NodeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraStrategyImpl implements IPathfindingStrategy {
    public RoadGraph mGraph;

    private final int inf = Integer.MAX_VALUE;
    private ArrayList<Vertex> mSetteledVertices;
    private final ArrayList<Integer> mObstacleList;

    public DijkstraStrategyImpl(RoadGraph graph, List<Coordinate> obstacleCoordinates) {
        this.mGraph = graph;
        this.mObstacleList = new ArrayList<>();

        if (obstacleCoordinates != null && graph != null && graph.mNodes != null) {
            Set<Integer> tempObstacleSet = new HashSet<>();
            for (Coordinate coordinate : obstacleCoordinates) {
                if (coordinate == null) continue;
                Integer id = NodeUtil.findNearestNodeIndex(
                        coordinate.getLatitude(),
                        coordinate.getLongitude(),
                        mGraph);
                if (id != null) {
                    tempObstacleSet.add(id);
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
                        tempObstacleSet.add(i);
                    }
                }
            }
            this.mObstacleList.addAll(tempObstacleSet);
        }
    }

    @Override
    public ArrayList<Integer> getShortestPath(int sourceNodeId, int targetNodeId) {
        if (mSetteledVertices == null || targetNodeId < 0 || targetNodeId >= mSetteledVertices.size() ||
                mSetteledVertices.get(targetNodeId) == null ||
                mSetteledVertices.get(targetNodeId).getDistance() == inf) {
            return null;
        }

        ArrayList<Integer> path = new ArrayList<>();
        int current = targetNodeId;

        while (mSetteledVertices.get(current) != null) {
            path.add(current);
            if (current == sourceNodeId) {
                break;
            }
            int parent = mSetteledVertices.get(current).getParent();
            if (parent == current) {
                System.err.println("Dijkstra Path Reconstruction Error: Node's parent is itself (node " + current + ")");
                return null;
            }
            current = parent;
            if (path.size() > mGraph.mNumNodes) {
                System.err.println("Dijkstra Path Reconstruction Error: Path too long.");
                return null;
            }
        }

        if (path.isEmpty() || path.getLast() != sourceNodeId) {
            if (sourceNodeId == targetNodeId && mSetteledVertices.get(sourceNodeId) != null && mSetteledVertices.get(sourceNodeId).getDistance() == 0) {
                if (path.isEmpty()) path.add(sourceNodeId);
            } else {
                // System.err.println("Dijkstra Path Reconstruction Error: Path does not end at source node.");
                return null;
            }
        }

        Collections.reverse(path);
        return path;
    }

    @Override
    public double computeShortestPathCost(int sourceNodeId, int targetNodeId) {
        if (mGraph == null || sourceNodeId < 0 || sourceNodeId >= mGraph.mNumNodes ||
                (targetNodeId != -1 && (targetNodeId < 0 || targetNodeId >= mGraph.mNumNodes)) ||
                mGraph.mNodes.get(sourceNodeId) == null ||
                (targetNodeId != -1 && mGraph.mNodes.get(targetNodeId) == null)) {
            System.err.println("Dijkstra Error: Invalid graph or source/target node ID.");
            return inf;
        }
        if (mObstacleList.contains(sourceNodeId) || (targetNodeId != -1 && mObstacleList.contains(targetNodeId))) {
            System.err.println("Dijkstra Error: Source or target node is an obstacle.");
            return inf;
        }
        PriorityQueue<Vertex> mPriorityQueue = new PriorityQueue<>(Comparator.comparingDouble(Vertex::getDistance));
        mSetteledVertices = new ArrayList<>(Collections.nCopies(mGraph.mNumNodes, null));
        double[] distTo = new double[mGraph.mNumNodes];
        for (int i = 0; i < mGraph.mNumNodes; i++) {
            distTo[i] = inf;
        }
        distTo[sourceNodeId] = 0;
        mPriorityQueue.add(new Vertex(sourceNodeId, 0, sourceNodeId));
        while (!mPriorityQueue.isEmpty()) {
            Vertex currentVertex = mPriorityQueue.poll();
            int u_id = currentVertex.getId();
            double gCost_u = currentVertex.getDistance();
            if (gCost_u > distTo[u_id]) {
                continue;
            }
            if (mSetteledVertices.get(u_id) != null) {
                continue;
            }
            mSetteledVertices.set(u_id, currentVertex);
            if (targetNodeId != -1 && u_id == targetNodeId) {
                return gCost_u;
            }
            if (mGraph.mOutgoingEdges.size() <= u_id || mGraph.mOutgoingEdges.get(u_id) == null) {
                continue;
            }
            for (Edge edge : mGraph.mOutgoingEdges.get(u_id)) {
                int v_id = edge.getHeadNode();
                if (mObstacleList.contains(v_id)) {
                    continue;
                }
                double newDistTo_v = gCost_u + edge.getTravelTime();
                if (newDistTo_v < distTo[v_id]) {
                    distTo[v_id] = newDistTo_v;
                    mPriorityQueue.add(new Vertex(v_id, newDistTo_v, u_id));
                }
            }
        }
        if (targetNodeId != -1) {
            Vertex targetInfo = (targetNodeId < mSetteledVertices.size()) ? mSetteledVertices.get(targetNodeId) : null;
            return (targetInfo != null && targetInfo.getDistance() != inf) ? targetInfo.getDistance() : inf;
        }

        return 0;
    }

    // static class DistanceComparator implements Comparator<Vertex> {
    //     @Override
    //     public int compare(Vertex vertex1, Vertex vertex2) {
    //         return Double.compare(vertex1.getDistance(), vertex2.getDistance());
    //     }
    // }
}