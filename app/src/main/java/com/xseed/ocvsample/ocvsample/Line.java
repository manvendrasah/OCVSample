package com.xseed.ocvsample.ocvsample;

import org.opencv.core.Point;

/**
 * Created by Manvendra Sah on 29/08/17.
 */

public class Line {

    Point p1, p2;
    double rho, theta;

    public Line(double x, double y, double x1, double y1) {
        p1 = new Point(x, y);
        p2 = new Point(x1, y1);
    }

    public Line(double x, double y, double x1, double y1, double rho, double theta) {
        p1 = new Point(x, y);
        p2 = new Point(x1, y1);
        this.rho = rho;
        this.theta = theta;
    }

    /* @Override
     public String toString() {
         return "" + p1.x + "," + p1.y + "  " + p2.x + "," + p2.y + ": rho = " + rho + ", theta = " + theta;
     }*/
    @Override
    public String toString() {
        return "" + (int) p1.x + "," + (int) p1.y + "  " + (int) p2.x + "," + (int) p2.y;
    }
}
