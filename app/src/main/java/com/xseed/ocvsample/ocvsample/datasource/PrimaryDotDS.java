package com.xseed.ocvsample.ocvsample.datasource;

import com.xseed.ocvsample.ocvsample.pojo.Dot;
import com.xseed.ocvsample.ocvsample.pojo.Line;

import org.opencv.core.Point;

/**
 * Created by Manvendra Sah on 05/09/17.
 */

public class PrimaryDotDS {

    public Dot topLeft, topRight, bottomLeft, bottomRight;
    private Line leftLine, bottomLine, topLine, rightLine;

    public void setTopLeft(int x, int y) {
        topLeft = new Dot(x, y, Dot.TOP_LEFT);
    }

    public void setTopRight(int x, int y) {
        topRight = new Dot(x, y, Dot.TOP_RIGHT);
    }

    public void setBottomRight(int x, int y) {
        bottomRight = new Dot(x, y, Dot.BOTTOM_RIGHT);
    }

    public void setBottomLeft(int x, int y) {
        bottomLeft = new Dot(x, y, Dot.BOTTOM_LEFT);
    }

    public void setTopLeft(double x, double y) {
        topLeft = new Dot(x, y, Dot.TOP_LEFT);
    }

    public void setTopRight(double x, double y) {
        topRight = new Dot(x, y, Dot.TOP_RIGHT);
    }

    public void setBottomRight(double x, double y) {
        bottomRight = new Dot(x, y, Dot.BOTTOM_RIGHT);
    }

    public void setBottomLeft(double x, double y) {
        bottomLeft = new Dot(x, y, Dot.BOTTOM_LEFT);
    }

    public boolean isValid() {
        return topLeft != null && topRight != null && bottomLeft != null && bottomRight != null;
    }

    public Line getTopLine() {
        if (topLine == null) {
            topLine = new Line(topLeft.x, topLeft.y, topRight.x, topRight.y);
        }
        return topLine;
    }

    public Line getRightLine() {
        if (rightLine == null) {
            rightLine = new Line(topRight.x, topRight.y, bottomRight.x, bottomRight.y);
        }
        return rightLine;
    }

    public Line getBottomLine() {
        if (bottomLine == null) {
            bottomLine = new Line(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
        }
        return bottomLine;
    }

    public Line getLeftLine() {
        if (leftLine == null) {
            leftLine = new Line(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y);
        }
        return leftLine;
    }

    public Point getTopLeftPoint() {
        return new Point(topLeft.x, topLeft.y);
    }

    public Point getTopRightPoint() {
        return new Point(topRight.x, topRight.y);
    }

    public Point getBottomLeftPoint() {
        return new Point(bottomLeft.x, bottomLeft.y);
    }

    public Point getBottomRightPoint() {
        return new Point(bottomRight.x, bottomRight.y);
    }

    @Override
    public String toString() {
        return "PrimaryDotDS {" +
                "topLeft=" + topLeft +
                ", topRight=" + topRight +
                ", bottomRight=" + bottomRight +
                ", bottomLeft=" + bottomLeft +
                "}  isValid=" + isValid() +
                '}';
    }
}
