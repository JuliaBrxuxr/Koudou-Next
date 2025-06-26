package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements;

public class Vertex {

	private int mId;
	private double mDistance;
	private int mParent;
	private double mHeuristic;

	public Vertex(int id, double distance, int parent) {
		mId = id;
		mDistance = distance;
		mParent = parent;
	}
	
	public Vertex(int id, double distance, int parent, double heuristic) {
		mId = id;
		mDistance = distance;
		mParent = parent;
		mHeuristic = heuristic;
    }

	public Vertex(Vertex vertex) {
		mId = vertex.getId();
		mDistance = vertex.getDistance();
		mParent = vertex.getParent();
		mHeuristic = vertex.getHeuristic();
    }

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public double getDistance() {
		return mDistance;
	}

	public void setDistance(double distance) {
		mDistance = distance;
	}

	public int getParent() {
		return mParent;
	}

	public void setParent(int parent) {
		mParent = parent;
	}

	public double getHeuristic() {
		return mHeuristic;
	}

	public void setHeuristic(double heuristic) {
		mHeuristic = heuristic;
	}
}