package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.RoadGraph;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;

import java.util.Comparator;
import java.util.PriorityQueue;

public class NodeUtil {
    public static Integer findNearestNodeIndex(double latitude, double longitude, RoadGraph graph) {
        double minDistance = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < graph.mNodes.size(); i++) {
            double distance = HaversineUtil.calculateDistance(latitude, longitude,
                    graph.mNodes.get(i).getCoordinate().getLatitude(),
                    graph.mNodes.get(i).getCoordinate().getLongitude());
            if (minDistance > distance) {
                minDistance = distance;
                index = i;
            }
        }
        return index;
    }

    public static PriorityQueue<NearNode> findNearNodes(double latitude, double longitude, RoadGraph graph) {
        PriorityQueue<NearNode> nearNodes = new PriorityQueue<>(Comparator.comparing(NearNode::getDistance));
        for (int i = 0; i < graph.mNodes.size(); i++) {
            double dist = HaversineUtil.calculateDistance(
                    latitude,
                    longitude,
                    graph.mNodes.get(i).getCoordinate().getLatitude(),
                    graph.mNodes.get(i).getCoordinate().getLongitude());
            nearNodes.add(new NearNode(i, dist));
        }
        return nearNodes;
    }

    public static class NearNode {
        private int mId;
        private double mDistance;

        public NearNode(int id, double distance) {
            mId = id;
            mDistance = distance;
        }

        public int getId() {
            return mId;
        }

        public double getDistance() {
            return mDistance;
        }

        public void setDistance(double distance) {
            mDistance = distance;
        }

        public void setId(int id) {
            mId = id;
        }
    }


}