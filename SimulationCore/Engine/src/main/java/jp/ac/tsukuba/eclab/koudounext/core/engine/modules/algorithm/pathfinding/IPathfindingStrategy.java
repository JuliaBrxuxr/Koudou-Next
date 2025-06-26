package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding;

import java.util.ArrayList;

public interface IPathfindingStrategy {
    public ArrayList<Integer> getShortestPath(int source, int target);
    public double computeShortestPathCost(int sourceNodeId, int targetNodeId);
}
