package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm;

import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Edge;
import jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements.Node;

import java.awt.geom.Point2D;
import java.util.*;

public class RoadGraph {
    String mRegionName;

    public int mNumNodes;
    public int mNumEdges;

    public ArrayList<ArrayList<Edge>> mOutgoingEdges;
    public ArrayList<ArrayList<Edge>> mIncomingEdges;

    private List<Point2D.Double> building;
    public ArrayList<Node> mNodes;
    public Map<Long, Integer> mOsmIdToNodeIndex;
    public ArrayList<String> mRoadTypes;

    public RoadGraph(String regionName) {
        mRegionName = regionName;
    }

    public void setBuildings(List<Point2D.Double> centroids) {
        this.building = centroids;
    }

    public List<Point2D.Double> getBuildings() {
        return this.building;
    }



    void addNodeInternal(long osmId, double latitude, double longitude) {
        if (!mOsmIdToNodeIndex.containsKey(osmId)) {
            Node node = new Node(osmId, longitude, latitude);
            mNodes.add(node);
            mOutgoingEdges.add(new ArrayList<>());
            mIncomingEdges.add(new ArrayList<>());
            mOsmIdToNodeIndex.put(osmId, this.mNumNodes);
            mNumNodes++;
        }
    }

    void addEdgeInternal(int baseNodeIndex, int headNodeIndex, double length, double travelTime) {
        if (baseNodeIndex >= 0 && baseNodeIndex < mNumNodes && headNodeIndex >= 0 && headNodeIndex < mNumNodes) {
            Edge outgoingEdge = new Edge(headNodeIndex, length, travelTime);
            Edge incomingEdge = new Edge(baseNodeIndex, length, travelTime);
            mOutgoingEdges.get(baseNodeIndex).add(outgoingEdge);
            mIncomingEdges.get(headNodeIndex).add(incomingEdge);
            mNumEdges++;
        }
    }

    public void reduceToLargestConnectedComponent() {
        if (mNumNodes == 0) {
            return;
        }

        // Backup original edges by deep copy.
        ArrayList<ArrayList<Edge>> originalOutgoingEdges = new ArrayList<>(mNumNodes);
        for (int i = 0; i < mNumNodes; i++) {
            ArrayList<Edge> edges = this.mOutgoingEdges.get(i);
            ArrayList<Edge> edgesCopy = new ArrayList<>();
            if (edges != null) {
                for (Edge edge : edges) {
                    edgesCopy.add(new Edge(edge.getHeadNode(), edge.getLength(), edge.getTravelTime()));
                }
            }
            originalOutgoingEdges.add(edgesCopy);
        }

        ArrayList<ArrayList<Edge>> augmentedOutgoingEdges = new ArrayList<>(mNumNodes);
        for (int i = 0; i < mNumNodes; i++) {
            augmentedOutgoingEdges.add(new ArrayList<>(this.mOutgoingEdges.get(i)));
        }

        for (int u = 0; u < mNumNodes; u++) {
            if (this.mIncomingEdges.get(u) == null) continue;
            for (Edge inEdge : this.mIncomingEdges.get(u)) {
                int v = inEdge.getHeadNode();
                boolean foundReverse = false;
                if (augmentedOutgoingEdges.get(u) != null) {
                    for (Edge outEdgeCheck : augmentedOutgoingEdges.get(u)) {
                        if (outEdgeCheck.getHeadNode() == v) {
                            foundReverse = true;
                            break;
                        }
                    }
                }
                if (!foundReverse) {
                    if (augmentedOutgoingEdges.get(u) == null) augmentedOutgoingEdges.set(u, new ArrayList<>());
                    augmentedOutgoingEdges.get(u).add(new Edge(v, inEdge.getLength(), inEdge.getTravelTime()));
                }
            }
        }

        ArrayList<Integer> componentMarkers = new ArrayList<>(Collections.nCopies(mNumNodes, 0));
        int currentRound = 0;
        int largestSize = 0;
        int largestMark = 0;

        for (int i = 0; i < mNumNodes; i++) {
            if (mNodes.get(i) == null || componentMarkers.get(i) != 0) {
                continue;
            }

            currentRound++;
            int countInCurrentComponent = 0;
            Queue<Integer> queue = new LinkedList<>();

            queue.add(i);
            componentMarkers.set(i, currentRound);
            countInCurrentComponent++;

            while (!queue.isEmpty()) {
                int u = queue.poll();
                if (augmentedOutgoingEdges.get(u) == null) continue;

                for (Edge edge : augmentedOutgoingEdges.get(u)) {
                    int v = edge.getHeadNode();
                    if (v >= 0 && v < mNumNodes && mNodes.get(v) != null && componentMarkers.get(v) == 0) {
                        componentMarkers.set(v, currentRound);
                        countInCurrentComponent++;
                        queue.add(v);
                    }
                }
            }

            if (countInCurrentComponent > largestSize) {
                largestSize = countInCurrentComponent;
                largestMark = currentRound;
            }
        }

        if (largestMark == 0 && mNumNodes > 0) {
            if (largestSize == 0) {
                // System.out.println("LCC: No components were marked. Retaining original graph or graph is empty/fragmented.");
                this.mOutgoingEdges = originalOutgoingEdges;
                return;
            }
            if (mNumNodes == 1) {
                largestMark = 1;
                if (!componentMarkers.isEmpty()) componentMarkers.set(0, 1);
                largestSize = 1;
            }
        }

        ArrayList<Node> newNodes = new ArrayList<>();
        Map<Long, Integer> newOsmIdToNodeIndex = new HashMap<>();
        ArrayList<Integer> oldToNewIndexMap = new ArrayList<>(Collections.nCopies(mNumNodes, -1));
        int newIndexCounter = 0;

        for (int oldIdx = 0; oldIdx < mNumNodes; oldIdx++) {
            if (mNodes.get(oldIdx) != null && componentMarkers.get(oldIdx) == largestMark) {
                Node node = mNodes.get(oldIdx);
                newNodes.add(node);
                newOsmIdToNodeIndex.put(node.getOsmId(), newIndexCounter);
                oldToNewIndexMap.set(oldIdx, newIndexCounter);
                newIndexCounter++;
            }
        }

        this.mNodes = newNodes;
        int oldNumNodesForEdgeIteration = this.mNumNodes;
        this.mNumNodes = newNodes.size();
        this.mOsmIdToNodeIndex = newOsmIdToNodeIndex;
        ArrayList<ArrayList<Edge>> newOutgoingEdges = new ArrayList<>(this.mNumNodes);
        ArrayList<ArrayList<Edge>> newIncomingEdges = new ArrayList<>(this.mNumNodes);
        for (int i = 0; i < this.mNumNodes; i++) {
            newOutgoingEdges.add(new ArrayList<>());
            newIncomingEdges.add(new ArrayList<>());
        }

        int newNumEdges = 0;
        for (int oldU = 0; oldU < oldNumNodesForEdgeIteration; oldU++) {
            int newU = oldToNewIndexMap.get(oldU);
            if (newU != -1) {
                if (oldU < originalOutgoingEdges.size() && originalOutgoingEdges.get(oldU) != null) {
                    for (Edge oldEdge : originalOutgoingEdges.get(oldU)) {
                        int oldV = oldEdge.getHeadNode();
                        int newV = (oldV >= 0 && oldV < oldToNewIndexMap.size()) ? oldToNewIndexMap.get(oldV) : -1;
                        if (newV != -1) {
                            newOutgoingEdges.get(newU).add(new Edge(newV, oldEdge.getLength(), oldEdge.getTravelTime()));
                            newNumEdges++;
                        }
                    }
                }
                if (oldU < this.mIncomingEdges.size() && this.mIncomingEdges.get(oldU) != null) {
                    for (Edge oldEdge : this.mIncomingEdges.get(oldU)) {
                        int oldV_source = oldEdge.getHeadNode();
                        int newV_source = (oldV_source >= 0 && oldV_source < oldToNewIndexMap.size()) ? oldToNewIndexMap.get(oldV_source) : -1;
                        if (newV_source != -1) {
                            newIncomingEdges.get(newU).add(new Edge(newV_source, oldEdge.getLength(), oldEdge.getTravelTime()));
                        }
                    }
                }
            }
        }

        this.mOutgoingEdges = newOutgoingEdges;
        this.mIncomingEdges = newIncomingEdges;
        this.mNumEdges = newNumEdges;
    }


    public ArrayList<Node> getAllNodes() {
        return mNodes;
    }

    public Node getNodeByIndex(Integer key) {
        if (key != null && key >= 0 && key < mNodes.size()) return mNodes.get(key);
        return null;
    }

    public int getNumNodes() {
        return mNumNodes;
    }

    public ArrayList<Edge> getOutgoingEdges(int nodeIndex) {
        if (nodeIndex >= 0 && nodeIndex < mOutgoingEdges.size()) return mOutgoingEdges.get(nodeIndex);
        return new ArrayList<>();
    }

    public ArrayList<Edge> getIncomingEdges(int nodeIndex) {
        if (nodeIndex >= 0 && nodeIndex < mIncomingEdges.size()) return mIncomingEdges.get(nodeIndex);
        return new ArrayList<>();
    }

    public int getNumEdges() {
        return mNumEdges;
    }

    public Map<Long, Integer> getOsmIdToNodeIndexMap() {
        return mOsmIdToNodeIndex;
    }

    public List<String> getRoadTypes() {
        return mRoadTypes;
    }
}