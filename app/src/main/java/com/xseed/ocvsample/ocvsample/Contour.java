package com.xseed.ocvsample.ocvsample;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

/**
 * Created by Manvendra Sah on 21/08/17.
 */

public class Contour {
    int id;
    double area;
    MatOfPoint mat;

    public Contour(int id, double area, MatOfPoint mat) {
        this.id = id;
        this.area = area;
        this.mat = mat;
    }
}
