package com.xseed.ocvsample.ocvsample.scanbase.helper.blob;

import android.graphics.Bitmap;
import android.support.v4.graphics.ColorUtils;

import com.xseed.ocvsample.ocvsample.scanbase.datasource.BlobDS;
import com.xseed.ocvsample.ocvsample.scanbase.datasource.CircleDS;
import com.xseed.ocvsample.ocvsample.scanbase.datasource.CircleRatios;
import com.xseed.ocvsample.ocvsample.scanbase.datasource.PrimaryDotDS;
import com.xseed.ocvsample.ocvsample.scanbase.pojo.Blob;
import com.xseed.ocvsample.ocvsample.scanbase.pojo.Circle;
import com.xseed.ocvsample.ocvsample.scanbase.utility.Logger;
import com.xseed.ocvsample.ocvsample.scanbase.utility.SheetConstants;

import org.opencv.core.Point;

import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 27/03/18.
 */

public abstract class AbstractBlobHelper {

    protected PrimaryDotDS dotData;
    protected CircleDS circleData;
    protected BlobDS blobData;
    protected CircleRatios cRatios;
    protected Bitmap bitmap;

    // template design pattern to assure proper functional calls
    public void findBlobsInFirstHalfCircles() {
        findGradeBlobs();
        findIdBlobs();
        findAnswersBlobs(1, 12);
    }

    public void findBlobsInSecondHalfCircles() {
        findAnswersBlobs(13);
    }

    public BlobDS getBlobData() {
        return blobData;
    }

    protected abstract void findGradeBlobs();

    protected abstract void findIdBlobs();

    protected abstract void findAnswersBlobs(int startIndex);

    protected abstract void findAnswersBlobs(int startIndex, int endIndex);

    protected double getDarknessInCircle(Circle ci, double radius) {
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

    protected synchronized Blob getDarkestCircleInList(ArrayList<Circle> list, int superInd, int type) {
        int maxInd = -1;
        double maxDarkness = 0;
        int len = list.size();
        for (int i = 0; i < len; ++i) {
            Circle ci = list.get(i);
            double darkness = getDarknessInCircle(ci, Math.ceil(cRatios.avgAnswerRadius));
            Logger.logOCV("Blob > circle > " + ci.toString() + " > " + superInd + " > " + i + " >  darkness = " + darkness);
            if (darkness > SheetConstants.MIN_DARKNESS && darkness > maxDarkness) {
                maxInd = i;
                maxDarkness = darkness;
            }
        }
        if (maxInd == -1) {
            return null;
        } else {
            Logger.logOCV("MAX BLOB > circle > " + list.get(maxInd).toString() + " > " + +superInd + " > " + maxInd + " >  darkness = " + maxDarkness);
            Blob blob = new Blob(list.get(maxInd), superInd, maxInd, type, maxDarkness);
            return blob;
        }
    }

    protected String getAlphaForAnswerSubIndex(int index) {
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

    protected Point getPointToRightForText(Blob blob) {
        Point p = blob.circle.center;
        int rad = (int) blob.circle.radius;
        Point point = new Point();
        point.x = p.x + 1.5 * rad;
        point.y = p.y + 1.0 * rad;
        return point;
    }

    protected Point getPointToBottomForText(Blob blob) {
        Point p = blob.circle.center;
        int rad = (int) blob.circle.radius;
        Point point = new Point();
        point.x = p.x - rad;
        point.y = p.y + 3 * rad;
        return point;
    }

    protected Point getTopLeftPointOfCircle(Blob blob) {
        Point p = blob.circle.center;
        double rad = blob.circle.radius;
        Point point = new Point();
        point.x = (int) (p.x - rad);
        point.y = (int) (p.y - rad);
        return point;
    }

    protected Point getBottomRightPointOfCircle(Blob blob) {
        Point p = blob.circle.center;
        double rad = blob.circle.radius;
        Point point = new Point();
        point.x = (int) (p.x + rad);
        point.y = (int) (p.y + rad);
        return point;
    }

    protected boolean isColorDark(int color) {
        return ColorUtils.calculateLuminance(color) <= SheetConstants.DARKNESS_THRESHHOLD;
    }
}
