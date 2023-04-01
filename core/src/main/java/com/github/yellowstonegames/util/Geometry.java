package com.github.yellowstonegames.util;

import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.BresenhamLine;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Region;

import java.util.List;

public class Geometry {
    public static Coord findCentroid(List<Coord> coords) {
        int centroidX = 0;
        int centroidY = 0;
        for (Coord c : coords) {
            centroidX += c.x;
            centroidY += c.y;
        }
        centroidX /= coords.size();
        centroidY /= coords.size();
        return Coord.get(centroidX, centroidY);
    }

    public static List<Coord> findInternalPolygonCorners(Region region, int distance, int pointLimit) {
        EnhancedRandom rng = new WhiskerRandom();
        rng.setState(region.hash64());
        Region points = region.copy();
        do {
            points.remake(region).randomScatter(rng, distance, 12);
            if (points.isEmpty()) {
                System.out.println("No points found for area");
            }
        } while (pointsInLine(points)); // need to make sure at least a triangle is possible

        QuickHull hull = new QuickHull();
        Coord[] coords = points.asCoords();
        return hull.executeQuickHull(coords);
    }

    public static Region connectPoints(Region region, Coord... points) {
        Region lines = region.copy();
        for (int i = 0; i < points.length; i++) {
            lines.addAll(BresenhamLine.line(points[i], points[(i + 1) % points.length]));
        }
        return lines;
    }

    public static Region connectPoints(Region region, List<Coord> points) {
        for (int i = 0; i < points.size(); i++) {
            region.addAll(BresenhamLine.line(points.get(i), points.get((i + 1) % points.size())));
        }
        return region;
    }

    public static boolean pointsInLine(Region points) {
        int sz = points.size();
        if (sz < 3) {
            return true; // 2 or less points are considered to always be in a line
        }

        double angle = Coord.degrees(points.nth(0), points.nth(1));
        for (int i = 1; i < sz; i++) {
            double test = Coord.degrees(points.nth(i), points.nth((i + 1) % sz));
            if (Math.abs((angle % 180) - (test % 180)) > 0.001) {
                return false;
            }
        }
        return true;
    }
}
