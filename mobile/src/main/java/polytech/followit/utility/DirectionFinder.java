package polytech.followit.utility;

public class DirectionFinder {

    public double angleBetweenTwoPoint(Point departure, Point arrival) {
        Point north = new Point(departure.x, departure.y + 1);
        Vector departure_arrival = vector(departure, arrival);
        Vector departure_north = vector(departure, north);
        double scalarProduct = scalarProduct(departure_arrival,departure_north);
        double magnitude = magnitude(departure_arrival)*magnitude(departure_north);
        return Math.acos(scalarProduct/magnitude);
    }

    /**
     * Create a vector from 2 points
     */
    public Vector vector(Point departure, Point arrival) {
        return new Vector(arrival.x - departure.x, arrival.y - departure.y);
    }

    public double scalarProduct(Vector v1, Vector v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public double magnitude(Vector v) {
        return Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2));
    }

    //==============================================================================================
    // Class implementation
    //==============================================================================================

    private class Vector {
        private double x;
        private double y;

        public Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private class Point {
        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}