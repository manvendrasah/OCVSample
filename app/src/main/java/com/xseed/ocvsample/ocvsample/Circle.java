package com.xseed.ocvsample.ocvsample;

import org.opencv.core.Point;

/**
 * Created by Manvendra Sah on 29/08/17.
 */

public class Circle {
    Point center;
    double radius;
    // int bottomDist = 0;
    int leftDist = 0;
    boolean isExtrapolated = false;

    public Circle(int centerX, int centerY, double radius) {
        this.center = new Point(centerX, centerY);
        this.radius = radius;
    }

    public Circle(double centerX, double centerY, double radius) {
        this.center = new Point(centerX, centerY);
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "(" + center.x + "," + center.y + ")" /*+ " [" + bottomDist + "] "*/;
    }
}
