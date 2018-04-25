package com.xseed.ocvsample.ocvsample.scanbase.helper;

import com.xseed.ocvsample.ocvsample.scanbase.utility.Logger;
import com.xseed.ocvsample.ocvsample.scanbase.datasource.LineDS;
import com.xseed.ocvsample.ocvsample.scanbase.pojo.Line;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 31/08/17.
 */

public class LineHelper {

    public LineDS sortLineList(ArrayList<Line> lines, Mat baseMat) {
        LineDS lineData;
        lineData = new LineDS();
        int maxY = Integer.MIN_VALUE, minY = Integer.MAX_VALUE,
                maxX = Integer.MIN_VALUE, minX = Integer.MAX_VALUE;
        Line top = null, bottom = null, left = null, right = null;

        for (Line line : lines) {
            Point p1 = line.p1;
            Point p2 = line.p2;

            if (line.rho < baseMat.cols() / 3 && p1.y > 0 && p2.y < 0 && p1.y > Math.abs(p1.x) && p1.x > 0)
                if (line.p1.x < minX) {
                    minX = (int) line.p1.x;
                    left = line;
                }

            if (line.rho < baseMat.cols() / 3 && p1.y < 0 && p2.y > 0 && p2.y > Math.abs(p2.x) && p2.x > 0)
                if (line.p2.x < minX) {
                    minX = (int) line.p2.x;
                    left = line;
                }

            if (p1.y > 0 && p2.y < 0 && p1.x > 0 && p2.x > 0)
                if (line.p1.x > maxX) {
                    maxX = (int) line.p1.x;
                    right = line;
                }

            if (p1.y < 0 && p2.y > 0 && p2.x > 0 && p1.x > 0)
                if (line.p2.x > maxX) {
                    maxX = (int) line.p2.x;
                    right = line;
                }

            if (p1.y > 0 && p2.y > 0 && p2.x > 0 && p1.x > 0 && p2.x > p1.x)
                if (line.p2.x > maxX) {
                    maxX = (int) line.p2.x;
                    right = line;
                }

            if (p1.y > 0 && p2.y > 0 && p2.x > 0 && p1.x > 0 && p1.x > p1.x)
                if (line.p1.x > maxX) {
                    maxX = (int) line.p1.x;
                    right = line;
                }

            if (line.rho < baseMat.rows() / 3 && p1.x > 0 && p2.x < 0 && p1.x > Math.abs(p1.y) && p1.y > 0)
                if (line.p1.y < minY) {
                    minY = (int) line.p1.y;
                    top = line;
                }

            if (line.rho < baseMat.rows() / 3 && p1.x < 0 && p2.x > 0 && p2.x > Math.abs(p2.y) && p2.y > 0)
                if (line.p2.y < minY) {
                    minY = (int) line.p2.y;
                    top = line;
                }

            if (p1.x > 0 && p2.x < 0 && p1.y > 0 && p2.y > 0)
                if (line.p1.y > maxX) {
                    maxX = (int) line.p1.y;
                    bottom = line;
                }

            if (p1.x < 0 && p2.x > 0 && p2.y > 0 && p1.y > 0)
                if (line.p2.y > maxX) {
                    maxX = (int) line.p2.y;
                    bottom = line;
                }
        }
        lineData.top = top;
        lineData.right = right;
        lineData.left = left;
        lineData.bottom = bottom;
        //  Logger.logOCV("# > image > " + baseMat.cols() + "," + baseMat.rows());
        Logger.logOCV("# > " + lineData.toString());
        return lineData;
    }

    public ArrayList<Line> findLines(Mat baseMat) {
        Mat linesMat = new Mat();
        Imgproc.cvtColor(baseMat, linesMat, Imgproc.COLOR_RGB2GRAY);
        //   Imgproc.threshold(linesMat, linesMat, 120, 240, Imgproc.THRESH_BINARY);
        //    Imgproc.adaptiveThreshold(linesMat, linesMat, 200, Imgproc.ADAPTIVE_THRESH_MEAN_C,
        //            Imgproc.THRESH_BINARY, 11, 12);
       /*     baseBitmap.recycle();
        baseBitmap = Bitmap.createBitmap(linesMat.cols(), linesMat.rows(), Config.ARGB_8888);
        Utils.matToBitmap(linesMat, baseBitmap);
        saveBitmapToDisk("lines", baseBitmap);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mSheetListener != null)
                    mSheetListener.onOMRSheetGradingComplete(baseBitmap);
            }
        });
        if (true)
            return;*/
        //   Imgproc.GaussianBlur(linesMat, linesMat, new org.opencv.core.Size(3, 3), 4);
        Imgproc.Canny(linesMat, linesMat, 80, 200);
        Mat lines = new Mat();
        Imgproc.HoughLines(linesMat, lines, 1, Math.PI / 180, 180);

        double[] data;
        double rho, theta;
        Point pt1 = new Point();
        Point pt2 = new Point();
        double a, b;
        double x0, y0;
        final ArrayList<Line> lineList = new ArrayList<Line>();
        for (int i = 0; i < lines.rows(); i++) {
            data = lines.get(i, 0);
            rho = data[0];
            theta = data[1];
            a = Math.cos(theta);
            b = Math.sin(theta);
            x0 = a * rho;
            y0 = b * rho;
            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * a);
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * a);
            // if (pt1.x < 0 && pt2.x > 0 /*&& Math.abs(pt1.y - pt2.y) < 200*/) {
            Line line = new Line(pt1.x, pt1.y, pt2.x, pt2.y, rho, theta);
            //    Logger.logOCV("# > points > " + line.toString());
            lineList.add(line);
            // Imgproc.line(baseMat, pt1, pt2, getScalar(i % 3), 3);
            // }
            //      Logger.logOCV("points > (" + pt1.x + "," + pt1.y + ") ,(" + pt2.x + "," + pt2.y + ")");
        }
        return lineList;
    }

}
