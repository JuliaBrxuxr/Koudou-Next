package jp.ac.tsukuba.eclab.koudounext.core.engine.modules.map.osm.utils;

public class HaversineUtil {
    // Approximate Earth radius, meter
    private static final int EARTH_RADIUS = 6371000;

    public static double calculateDistance(
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude) {

        double differenceLatitudeInRadians = Math.toRadians((endLatitude - startLatitude));
        double differenceLongitudeInRadians = Math.toRadians((endLongitude - startLongitude));

        double startLatitudeRadians = Math.toRadians(startLatitude);
        double endLatitudeRadians = Math.toRadians(endLatitude);

        double squaredSineOfHalfAngularDistance = calculateHaversine(differenceLatitudeInRadians) +
                Math.cos(startLatitudeRadians) *
                        Math.cos(endLatitudeRadians) *
                        calculateHaversine(differenceLongitudeInRadians);
        double centralAngleRadians = 2 * Math.atan2(
                Math.sqrt(squaredSineOfHalfAngularDistance),
                Math.sqrt(1 - squaredSineOfHalfAngularDistance));

        return EARTH_RADIUS * centralAngleRadians;
    }

    private static double calculateHaversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
