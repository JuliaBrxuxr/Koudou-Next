package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.test;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.IPathfindingStrategy;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.impl.AStarStrategyImpl;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.impl.BFSStrategyImpl;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.algorithm.pathfinding.impl.DFSStrategyImpl;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraphBuilder;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.NodeUtil;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils.BuildingCreator;


import java.awt.geom.Point2D;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public class MapTest {
    public MapTest() {
        String osmFilepath = "/osm/Tx-To-TU.osm";
//        String osmFilepath = "/osm/map.osm";
        String region = "tsukuba";
        double startLongitude = 140.10224191218825;
        double startLatitude = 36.092447120676034;
        double endLongitude = 140.0997849870918;
        double endLatitude = 36.11113443681083;
        
        BuildingCreator buildingCreator = new BuildingCreator();
        InputStream osmInputStream = this.getClass().getResourceAsStream(osmFilepath);
        List<Point2D.Double> buildingCentroids = buildingCreator.parse(osmInputStream);
        

      
        RoadGraph graph;
        try {
            graph = new RoadGraphBuilder()
                    .setOsmInputStream(this.getClass().getResourceAsStream(osmFilepath))
                    .setRegionName(region)
                    .build();
            System.out.println("Nodes before LCC: " + graph.getAllNodes().size());
            graph.reduceToLargestConnectedComponent();
            System.out.println("Nodes after LCC: " + graph.getAllNodes().size());
        } catch (Exception e) {
            System.err.println("Error loading map: " + e.getMessage());
//            e.printStackTrace();
            return;
        }

        PriorityQueue<NodeUtil.NearNode> startNodesQueue = NodeUtil.findNearNodes(startLatitude, startLongitude, graph);
        PriorityQueue<NodeUtil.NearNode> endNodesQueue = NodeUtil.findNearNodes(endLatitude, endLongitude, graph);
        ArrayList<Integer> shortestPath = new ArrayList<>();
        double pathCost = Double.MAX_VALUE;
        if (startNodesQueue.isEmpty() || endNodesQueue.isEmpty()) {
            System.err.println("Could not find any near nodes for start or end coordinates.");
        } else {
            NodeUtil.NearNode startNode = startNodesQueue.poll();
            NodeUtil.NearNode endNode = endNodesQueue.poll();

            if (startNode == null || endNode == null) {
                System.err.println("Failed to get a start or end node from the queue.");
            } else {
                System.out.println("Calculating path from nearest start node ID: " + startNode.getId() +
                        " to nearest end node ID: " + endNode.getId());

//                IPathfindingStrategy pathfinding = new AStarStrategyImpl(graph, new ArrayList<>());
//                IPathfindingStrategy pathfinding = new DijkstraStrategyImpl(graph, new ArrayList<>());
//                IPathfindingStrategy pathfinding = new IDAStarStrategyImpl(graph, new ArrayList<>());
//                IPathfindingStrategy pathfinding = new BFSStrategyImpl(graph, new ArrayList<>());
                IPathfindingStrategy pathfinding = new DFSStrategyImpl(graph, new ArrayList<>());
                pathCost = pathfinding.computeShortestPathCost(startNode.getId(), endNode.getId());
                ArrayList<Integer> calculatedPath = pathfinding.getShortestPath(startNode.getId(), endNode.getId());
                if (calculatedPath != null && !calculatedPath.isEmpty()) {
                    shortestPath = calculatedPath;
                    System.out.println("Path found with cost: " + pathCost);
                } else {
                    System.out.println("No path found between the selected nearest start and end nodes. Cost: " + pathCost);
                    pathCost = Double.MAX_VALUE;
                }
            }
        }

        System.out.println("Shortest path node indices: " + shortestPath);
        new RoadNetworkViewer(graph, shortestPath, buildingCentroids, "A Star (Single Nearest Pair)");
    }

    public static void main(String[] args) {
        new MapTest();
    }
}