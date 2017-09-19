package com.xseed.ocvsample.ocvsample.datasource;

import com.xseed.ocvsample.ocvsample.pojo.Line;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Manvendra Sah on 30/08/17.
 */

public class LineDS {

    public Line top, bottom, left, right; // lines at extremeties
    public Line hor1, hor2, hor3; // horizontal dividing lines for answers
    public Line ver1, ver2, ver3; // vertical dividing lines for answers

    public boolean isInSufficient() {
        //return false;
        // return top == null || bottom == null || left == null || right == null;
        return bottom == null || left == null;
    }

    public void drawOnMat(Mat baseMat) {
        if (top != null)
            Imgproc.line(baseMat, top.p1, top.p2, new Scalar(255, 0, 0), 4);
        if (right != null)
            Imgproc.line(baseMat, right.p1, right.p2, new Scalar(0, 255, 0), 4);
        if (bottom != null)
            Imgproc.line(baseMat, bottom.p1, bottom.p2, new Scalar(0, 0, 255), 4);
        if (left != null)
            Imgproc.line(baseMat, left.p1, left.p2, new Scalar(240, 240, 30), 4);

        //  Imgproc.putText(baseMat, "T1", top.p1, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 4);
        //  Imgproc.putText(baseMat, "T2", top.p2, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 4);
    }

    @Override
    public String toString() {
        return "LineDS {" +
                "top= [ " + top +
                " ] , bottom= [ " + bottom +
                " ], left= [ " + left +
                " ], right= [ " + right + " ]";
               /* ", hor1=" + hor1 +
                ", hor2=" + hor2 +
                ", hor3=" + hor3 +
                ", ver1=" + ver1 +
                ", ver2=" + ver2 +
                ", ver3=" + ver3 +
                '}';*/
    }
}
