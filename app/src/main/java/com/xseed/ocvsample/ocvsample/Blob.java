package com.xseed.ocvsample.ocvsample;

import org.opencv.core.Point;

/**
 * Created by Manvendra Sah on 15/09/17.
 */

public class Blob {

    Circle circle;
    int superIndex; // index of row or column
    int index; // index within row or column
    int type;
    double darkness;

    public Blob(Circle circle, int superIndex, int index, int type, double darkness) {
        this.circle = circle;
        this.superIndex = superIndex;
        this.index = index;
        this.type = type;
        this.darkness = darkness;
    }

    @Override
    public String toString() {
        return "Blob{" +
                "circle=" + circle +
                ", superIndex=" + superIndex +
                ", index=" + index +
                ", type=" + type +
                ", darkness=" + darkness +
                '}';
    }
}
