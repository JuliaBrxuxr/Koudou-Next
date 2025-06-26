package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.elements;

public class Coordinate {
    private double mLongitude;
    private double mLatitude;

    public Coordinate(double longitude, double latitude){
        mLongitude = longitude;
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    @Override
    public String toString() {
        return ("Longitude: " + mLongitude + ", Latitude: " + mLatitude);
    }
}
