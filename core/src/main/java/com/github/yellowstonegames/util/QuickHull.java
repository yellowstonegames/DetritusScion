package com.github.yellowstonegames.util;

import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * QuickHull is an algorithm to compute the convex hull of a set of points. The time complexity is O(n^2) in the worst
 * case and O(n*log n) on average.
 * <br>
 * Based on <a href="https://github.com/Thurion/algolab">algolab</a> and modified to work in the SquidSquad ecosystem.
 */
public class QuickHull {

    /**
     * Implementation of the QuickHull algorithm.
     *
     * @param inputPoints The points to find a hull around
     * @return A list of points which form the convex hull of the given list of points.
     */
    public ObjectList<Coord> executeQuickHull(Region inputPoints) {
        ObjectList<Coord> convexHull = new ObjectList<>();
        if (inputPoints == null || inputPoints.size() < 1) {
            throw new IllegalArgumentException("Cannot compute convex hull of zero points.");
        }

        // search extreme values
        Coord rightmostPoint = inputPoints.first();
        Coord leftmostPoint = inputPoints.first();
        for (Coord point : inputPoints) {
            if (point.getX() < rightmostPoint.getX()) {
                rightmostPoint = point;
            } else if (point.getX() > leftmostPoint.getX()) {
                leftmostPoint = point;
            }
        }

        // divide the set into two halves
        ObjectList<Coord> leftOfLine = new ObjectList<>();
        ObjectList<Coord> rightOfLine = new ObjectList<>();
        for (Coord point : inputPoints) {
            if (point.equals(rightmostPoint) || point.equals(leftmostPoint)) {
                continue;
            }

            if (isLeftOfLine(point, leftmostPoint, rightmostPoint)) {
                leftOfLine.add(point);
            } else {
                rightOfLine.add(point);
            }
        }

        convexHull.add(leftmostPoint);
        ObjectList<Coord> hull = divide(leftOfLine, leftmostPoint, rightmostPoint);
        convexHull.addAll(hull);
        convexHull.add(rightmostPoint);

        hull = divide(rightOfLine, rightmostPoint, leftmostPoint);
        convexHull.addAll(hull);
        return convexHull;
    }

    /**
     * Calculate the cross product of vectors origin->p2 and origin->this.
     * Because origin, p1, and p2 are all integer-component Coord items, this returns an int.
     *
     * @param origin The point in which both vectors originate
     * @param p2 The point that determines the second vector.
     * @return 0 if both points are collinear, a value > 0 if this point lies left of vector origin->p2 (when standing
     * in origin looking at p2), a value < 0 if this point lies right of vector origin->p2.
     */
    private int calcCrossProductWithOrigin(Coord origin, Coord p1, Coord p2) {
        return (p2.x - origin.x) * (p1.y - origin.y)
            - (p2.y - origin.y) * (p1.x - origin.x);
    }

    /**
     * A point is considered left of a line between points from and to if it is on the lefthand side when looking along
     * the line from point "from" to point "to".
     *
     * The method uses the cross-product to determine if this point is left of the line.
     *
     * @param point The point to check
     * @param from Point from which the line is drawn and from where we "look" along the line in direction of point "to"
     * to determine whether the point is left or right of it.
     * @param to Point to which the line is drawn
     */
    private boolean isLeftOfLine(Coord point, Coord from, Coord to) {
        return Double.compare(calcCrossProductWithOrigin(from, point, to), 0) > 0;
    }

    /**
     * Calculates the distance of this point to the line which is formed by points a and b.
     *
     * @param point The point to calculate the distance for
     * @param start The start of the line
     * @param end The end of the line
     * @return The distance to the line.
     */
    private double getDistanceToLine(Coord point, Coord start, Coord end) {
        return Math.abs((end.getX() - start.getX()) * (start.getY() - point.y) - (start.getX() - point.x) * (end.getY() - start.getY()))
            / Math.sqrt(MathTools.square(end.getX() - start.getX()) + MathTools.square(end.getY() - start.getY()));
    }

    /**
     * Finds and returns the list of points that are to the "left" of the line p1-p2. What is "left" is determined by
     * the two points passed in as they define the dividing line.
     *
     * @param points The list of points
     * @param top The "top" point  for splitting left of
     * @param bottom The "bottom" point for splitting left of
     * @return a List of points to the "left" of the line p1-p2.
     * If the list is empty, the line p1-p2 is the convex hull.
     * If p1 is to the right of p2, this vertically mirrors the list, causing the "left" that's returned to be the "right" of the original list.
     */
    private ObjectList<Coord> divide(ObjectList<Coord> points, Coord top, Coord bottom) {

        ObjectList<Coord> hull = new ObjectList<>();

        if (points.isEmpty()) {
            return hull;
        } else if (points.size() == 1) {
            hull.add(points.first());
            return hull;
        }

        Coord maxDistancePoint = points.get(0);
        ObjectList<Coord> l1 = new ObjectList<>();
        ObjectList<Coord> l2 = new ObjectList<>();
        double distance = 0.0;
        for (Coord point : points) {
            if (getDistanceToLine(point, top, bottom) > distance) {
                distance = getDistanceToLine(point, top, bottom);
                maxDistancePoint = point;
            }
        }

        points.remove(maxDistancePoint);

        for (Coord point : points) {
            if (isLeftOfLine(point, top, maxDistancePoint)) {
                l1.add(point);
            } else if (isLeftOfLine(point, maxDistancePoint, bottom)) {
                l2.add(point);
            }
        }

        points.clear();

        ObjectList<Coord> hullPart = divide(l1, top, maxDistancePoint);
        hull.addAll(hullPart);
        hull.add(maxDistancePoint);
        hullPart = divide(l2, maxDistancePoint, bottom);
        hull.addAll(hullPart);

        return hull;
    }
}
