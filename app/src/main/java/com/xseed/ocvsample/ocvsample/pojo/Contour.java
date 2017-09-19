package com.xseed.ocvsample.ocvsample.pojo;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

/**
 * Created by Manvendra Sah on 21/08/17.
 */

public class Contour {
    public int id;
    public double area;
    public MatOfPoint mat;

    public Contour(int id, double area, MatOfPoint mat) {
        this.id = id;
        this.area = area;
        this.mat = mat;
    }
}
