package com.xseed.ocvsample.ocvsample.pojo;

/**
 * Created by Manvendra Sah on 29/08/17.
 */

public class OCVCircleConfig {
    // accumulator value, lesser value is good for filtering extra circles
    public double dp;
    // minimum distance between the center coordinates of detected circles in pixels
    public int minDist;
    // min and max radii
    public int minRadius;
    public int maxRadius;
    // param1 = gradient value used to handle edge detection > more circles get detected if this decreases
    // param2 = Accumulator threshold value for the
    // cv2.CV_HOUGH_GRADIENT method.
    // The smaller the threshold is, the more circles will be
    // detected (including false circles).
    // The larger the threshold is, the lesser circles will
    // potentially be returned.
    public int param1;
    public int param2;
    // Rectangle parameters for subrect
    public int topLeftX;
    public int topLeftY;
    public int rectWidth;
    public int rectHeight;
}
