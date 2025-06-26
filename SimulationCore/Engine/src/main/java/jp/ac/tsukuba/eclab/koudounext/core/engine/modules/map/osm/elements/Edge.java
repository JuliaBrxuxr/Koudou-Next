package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements;

public class Edge {
	private int mHeadNode;
	private double mLength;
	private double mTravelTime;
	
	public Edge(int headNode, double length, double travelTime) {
		mHeadNode = headNode;
		mLength = length;
		mTravelTime = travelTime;
	}


	public int getHeadNode() {
		return mHeadNode;
	}

	public void setHeadNode(int headNode) {
		mHeadNode = headNode;
	}

	public double getLength() {
		return mLength;
	}

	public void setLength(double length) {
		mLength = length;
	}

	public double getTravelTime() {
		return mTravelTime;
	}

	public void setTravelTime(double travelTime) {
		mTravelTime = travelTime;
	}
}
