package com.fyp.autologistics.util;

public final class GeoDistanceUtils {

    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final double KM_PER_DEGREE_LATITUDE = 111.32;

    private GeoDistanceUtils() {
    }

    public static double haversineDistanceKm(
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude
    ) {
        double latitudeDistance = Math.toRadians(endLatitude - startLatitude);
        double longitudeDistance = Math.toRadians(endLongitude - startLongitude);
        double startLatitudeRadians = Math.toRadians(startLatitude);
        double endLatitudeRadians = Math.toRadians(endLatitude);

        double haversine =
                Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                        + Math.cos(startLatitudeRadians)
                        * Math.cos(endLatitudeRadians)
                        * Math.sin(longitudeDistance / 2)
                        * Math.sin(longitudeDistance / 2);

        return 2 * EARTH_RADIUS_KM * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    public static double distanceFromPointToSegmentKm(
            double pointLatitude,
            double pointLongitude,
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude
    ) {
        double referenceLatitude = (startLatitude + endLatitude) / 2.0;
        double[] point = toKmCoordinates(pointLatitude, pointLongitude, referenceLatitude);
        double[] start = toKmCoordinates(startLatitude, startLongitude, referenceLatitude);
        double[] end = toKmCoordinates(endLatitude, endLongitude, referenceLatitude);

        double segmentX = end[0] - start[0];
        double segmentY = end[1] - start[1];
        double segmentLengthSquared = segmentX * segmentX + segmentY * segmentY;

        if (segmentLengthSquared == 0) {
            return haversineDistanceKm(pointLatitude, pointLongitude, startLatitude, startLongitude);
        }

        double progress =
                ((point[0] - start[0]) * segmentX + (point[1] - start[1]) * segmentY)
                        / segmentLengthSquared;
        double clampedProgress = clamp(progress, 0.0, 1.0);
        double closestX = start[0] + clampedProgress * segmentX;
        double closestY = start[1] + clampedProgress * segmentY;

        return Math.hypot(point[0] - closestX, point[1] - closestY);
    }

    public static double progressRatioOnSegment(
            double pointLatitude,
            double pointLongitude,
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude
    ) {
        double referenceLatitude = (startLatitude + endLatitude) / 2.0;
        double[] point = toKmCoordinates(pointLatitude, pointLongitude, referenceLatitude);
        double[] start = toKmCoordinates(startLatitude, startLongitude, referenceLatitude);
        double[] end = toKmCoordinates(endLatitude, endLongitude, referenceLatitude);

        double segmentX = end[0] - start[0];
        double segmentY = end[1] - start[1];
        double segmentLengthSquared = segmentX * segmentX + segmentY * segmentY;

        if (segmentLengthSquared == 0) {
            return 0.0;
        }

        double progress =
                ((point[0] - start[0]) * segmentX + (point[1] - start[1]) * segmentY)
                        / segmentLengthSquared;

        return clamp(progress, 0.0, 1.0);
    }

    private static double[] toKmCoordinates(
            double latitude,
            double longitude,
            double referenceLatitude
    ) {
        double x = longitude
                * KM_PER_DEGREE_LATITUDE
                * Math.cos(Math.toRadians(referenceLatitude));
        double y = latitude * KM_PER_DEGREE_LATITUDE;

        return new double[] { x, y };
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
