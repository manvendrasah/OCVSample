package com.xseed.ocvsample.ocvsample.pojo;

/**
 * Created by Manvendra Sah on 05/09/17.
 */

public class Dot {

    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 1;
    public static final int BOTTOM_RIGHT = 2;
    public static final int BOTTOM_LEFT = 3;
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

    @Override
    public String toString() {
        return "Dot{" +
                "x,y =" + x +
                "," + y +
                ", type=" + type +
                '}';
    }
}
