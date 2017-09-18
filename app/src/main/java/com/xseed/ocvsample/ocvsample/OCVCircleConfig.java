package com.xseed.ocvsample.ocvsample;

/**
 * Created by Manvendra Sah on 29/08/17.
 */

public class OCVCircleConfig {
    // accumulator value
    double dp;
    // minimum distance between the center coordinates of detected circles in pixels
    int minDist;
    // min and max radii
     int minRadius;
    int maxRadius;
    // param1 = gradient value used to handle edge detection
    // param2 = Accumulator threshold value for the
    // cv2.CV_HOUGH_GRADIENT method.
    // The smaller the threshold is, the more circles will be
    // detected (including false circles).
    // The larger the threshold is, the more circles will
    // potentially be returned.
     int param1;
    int param2;
    // Rectangle parameters for subrect
    int topLeftX;
    int topLeftY;
    int rectWidth;
    int rectHeight;
}
