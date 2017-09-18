package com.xseed.ocvsample.ocvsample;

import android.graphics.Bitmap;
import android.support.v4.graphics.ColorUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Manvendra Sah on 15/09/17.
 */

public class BlobHelper {

    private DotDS dotData;
    private CircleDS circleData;
    private Bitmap bitmap;
    private Mat mat;
    private BlobDS blobData;
    private CircleRatios cRatios;

    public BlobHelper(DotDS dotData, CircleDS circleData, Mat mat, Bitmap bitmap, CircleRatios cRatios) {
        this.dotData = dotData;
        this.circleData = circleData;
        this.mat = mat;
        this.cRatios = cRatios;
        this.bitmap = bitmap;
        blobData = new BlobDS();
    }

    public BlobDS findBlobsInCircles() {
        findGradeBlobs();
        findIdBlobs();
        findAnswersBlobs();
        return blobData;
    }

    private void findAnswersBlobs() {
        Logger.logOCV("PENCIL DETECTION IN ANSWERS -----------------");
        Set<Integer> set = circleData.transwerCircleMap.keySet();
        for (Integer i : set) {
            Blob blob = getDarkestCircleInList(circleData.transwerCircleMap.get(i), i, SheetConstants.TYPE_ANSWER);
            if (blob != null)
                blobData.setAnswerBlob(blob);
        }
    }

    private void findIdBlobs() {
        Logger.logOCV("PENCIL DETECTION IN IDS -----------------");
        int ind = 0;
        for (ArrayList<Circle> list : circleData.idCircleMap.values()) {
            Blob blob = getDarkestCircleInList(list, ind, SheetConstants.TYPE_ID);
            if (blob != null)
                blobData.setIdBlob(blob);
            ++ind;
        }
    }

    private void findGradeBlobs() {
        Logger.logOCV("PENCIL DETECTION IN GRADE -----------------");
        ArrayList<Circle> list = circleData.gradeCircleMap.get(0);
        Blob blob = getDarkestCircleInList(list, 0, SheetConstants.TYPE_GRADE);
        if (blob != null)
            blobData.setGradeBlob(blob);
    }

    private Blob getDarkestCircleInList(ArrayList<Circle> list, int superInd, int type) {
        int maxInd = -1;
        double maxDarkness = 0;
        int len = list.size();
        for (int i = 0; i < len; ++i) {
            Circle ci = list.get(i);
            double darkness = getDarknessInCircle(ci, Math.ceil(cRatios.avgAnswerRadius));
            Logger.logOCV("Blob > circle > " + superInd + " > " + i + " >  darkness = " + darkness);
            if (darkness > SheetConstants.MIN_DARKNESS && darkness > maxDarkness) {
                maxInd = i;
                maxDarkness = darkness;
            }
        }
        if (maxInd == -1) {
            return null;
        } else {
            Logger.logOCV("MAX BLOB > circle > " + superInd + " > " + maxInd + " >  darkness = " + maxDarkness + " > for > " + list.get(maxInd).toString());
            Blob blob = new Blob(list.get(maxInd), superInd, maxInd, type, maxDarkness);
            return blob;
        }
    }

    private double getDarknessInCircle(Circle ci, double radius) {
        Point center = ci.center;
        radius /= 1.5; // check in square inside circle only, not covering circumference as root(2) = 1.414d -> 1.5d is used
        int x1 = (int) (center.x - radius);
        int y1 = (int) (center.y - radius);
        int x2 = (int) (center.x + radius);
        int y2 = (int) (center.y + radius);
        double count = 0;
        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; ++j) {
                if (isColorDark(bitmap.getPixel(i, j))) {
                    count += 1.0;
                }
            }
        }
        return count;
    }

    public boolean isColorDark(int color) {
        return ColorUtils.calculateLuminance(color) <= 0.1d;
    }

    public void drawBlobsOnMat() {
        for (Blob blob : blobData.gradeBlobs) {
            Imgproc.circle(mat, blob.circle.center, (int) Math.ceil(cRatios.avgIdGradeRadius), new Scalar(10, 255, 10), 2);
            //Imgproc.rectangle(mat, getTopLeftPointOfCircle(blob), getBottomRightPointOfCircle(blob), new Scalar(10, 255, 10), 2);
            Imgproc.putText(mat, String.valueOf(blob.index), getPointToRightForText(blob), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(54, 31, 200), 2);
        }
        for (Blob blob : blobData.idBlobs) {
            Imgproc.circle(mat, blob.circle.center, (int) Math.ceil(cRatios.avgIdGradeRadius), new Scalar(10, 255, 10), 2);
            //Imgproc.rectangle(mat, getTopLeftPointOfCircle(blob), getBottomRightPointOfCircle(blob), new Scalar(10, 255, 10), 2);
            Imgproc.putText(mat, String.valueOf(blob.index), getPointToRightForText(blob), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(54, 31, 200), 2);
        }
        for (Blob blob : blobData.answerBlobs) {
            Imgproc.circle(mat, blob.circle.center, (int) Math.ceil(cRatios.avgAnswerRadius), new Scalar(10, 255, 10), 2);
            //Imgproc.rectangle(mat, getTopLeftPointOfCircle(blob), getBottomRightPointOfCircle(blob), new Scalar(10, 255, 10), 2);
            Imgproc.putText(mat, getAlphaForAnswerSubIndex(blob.index), getPointToBottomForText(blob), Core.FONT_HERSHEY_COMPLEX_SMALL, 1.15, new Scalar(54, 31, 200), 2);
        }
    }

    private String getAlphaForAnswerSubIndex(int index) {
        switch (index) {
            case 0:
                return "A";
            case 1:
                return "B";
            case 2:
                return "C";
            case 3:
                return "D";
            default:
                return "X";
        }
    }

    public Point getPointToRightForText(Blob blob) {
        Point p = blob.circle.center;
        int rad = (int) blob.circle.radius;
        Point point = new Point();
        point.x = p.x + 1.5 * rad;
        point.y = p.y + 1.0 * rad;
        return point;
    }

    public Point getPointToBottomForText(Blob blob) {
        Point p = blob.circle.center;
        int rad = (int) blob.circle.radius;
        Point point = new Point();
        point.x = p.x - rad;
        point.y = p.y + 3 * rad;
        return point;
    }

    private Point getTopLeftPointOfCircle(Blob blob) {
        Point p = blob.circle.center;
        double rad = blob.circle.radius;
        Point point = new Point();
        point.x = (int) (p.x - rad);
        point.y = (int) (p.y - rad);
        return point;
    }

    private Point getBottomRightPointOfCircle(Blob blob) {
        Point p = blob.circle.center;
        double rad = blob.circle.radius;
        Point point = new Point();
        point.x = (int) (p.x + rad);
        point.y = (int) (p.y + rad);
        return point;
    }
}
