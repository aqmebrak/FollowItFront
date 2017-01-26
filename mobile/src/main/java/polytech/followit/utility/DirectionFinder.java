package polytech.followit.utility;

import polytech.followit.model.Node;

public class DirectionFinder {

    public static double angleBetweenTwoNode(Node departure, Node arrival) {
        Point departurePoint = new Point(departure.getxCoord(), departure.getyCoord());
        Point arrivalPoint = new Point(arrival.getxCoord(), arrival.getyCoord());
        Point northPoint = new Point(departure.getxCoord(), departure.getyCoord() - 1);
        Vector departure_arrival = vector(departurePoint, arrivalPoint);
        Vector departure_north = vector(departurePoint, northPoint);
        double scalarProduct = scalarProduct(departure_arrival,departure_north);
        double magnitude = magnitude(departure_arrival)*magnitude(departure_north);
        return Math.toDegrees(Math.acos(scalarProduct/magnitude));
    }

    /** Create a vector from 2 points**/
    public static Vector vector(Point departure, Point arrival) {
        return new Vector(arrival.x - departure.x, arrival.y - departure.y);
    }

    private static double scalarProduct(Vector v1, Vector v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    private static double magnitude(Vector v) {
        return Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2));
    }

    //==============================================================================================
    // Class implementation
    //==============================================================================================

    private static class Vector {
        private double x;
        private double y;

        Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Point {
        private double x;
        private double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}