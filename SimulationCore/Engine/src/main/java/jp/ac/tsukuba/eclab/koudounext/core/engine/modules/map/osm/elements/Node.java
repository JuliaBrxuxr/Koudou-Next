package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements;

public class Node {
	private long mOsmId;
	private Coordinate mCoordinate;
	
	public Node(long osmId, double longitude,double latitude) {
		mOsmId = osmId;
		mCoordinate = new Coordinate(longitude,latitude);
	}

	public long getOsmId() {
		return mOsmId;
	}

	public void setOsmId(long osmId) {
		mOsmId = osmId;
	}

	public Coordinate getCoordinate() {
		return mCoordinate;
	}

	public void setCoordinate(double latitude, double longitude) {
		mCoordinate = new Coordinate(latitude, longitude);
	}

	@Override
	public String toString() {
		return ("OSM ID: " + mOsmId + ", Coordinate: " + mCoordinate.toString());
	}
}
