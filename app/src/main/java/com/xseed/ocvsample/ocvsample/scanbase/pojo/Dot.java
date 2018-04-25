package com.xseed.ocvsample.ocvsample.scanbase.pojo;

/**
 * Created by Manvendra Sah on 05/09/17.
 */

public class Dot {

    // primary boundary dots types
    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 1;
    public static final int BOTTOM_RIGHT = 2;
    public static final int BOTTOM_LEFT = 3;
    // secondary identity dots common type
    public static final int OTHER = 4;

    public int x;
    public int y;
    public int type;

    public Dot(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public Dot(double x, double y, int type) {
        this.x = (int) x;
        this.y = (int) y;
        this.type = type;
    }

    public Dot(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = OTHER;
    }

    public Dot(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
        this.type = OTHER;
    }

    @Override
    public String toString() {
        return "Dot{" +
                "x,y =" + x +
                "," + y +
                ", type=" + type +
                '}';
    }
}
