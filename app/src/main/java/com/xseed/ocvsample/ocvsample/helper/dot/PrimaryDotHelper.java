package com.xseed.ocvsample.ocvsample.helper.dot;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.xseed.ocvsample.ocvsample.datasource.PrimaryDotDS;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.utility.SheetConstants;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Manvendra Sah on 05/09/17.
 */

public class PrimaryDotHelper extends BaseDotHelper {

    private PrimaryDotDS dotData;
    private Bitmap bitmap;
    private int searchLength;

    public PrimaryDotDS findDots(Bitmap bitmap) {
        dotData = new PrimaryDotDS();
        this.bitmap = bitmap;
        this.searchLength = bitmap.getWidth() / SheetConstants.SEARCH_LENGTH_DIVISOR;
        findBoundaryDots();
        return dotData;
    }

    /* find the corner dots by looping through pixel data */
    public void findBoundaryDots() {
        int hp = (int) (10 * SheetConstants.SIZEFACTOR);
        int wp = (int) (10 * SheetConstants.SIZEFACTOR);
        double sizeFactor = SheetConstants.SIZEFACTOR;
        double ap = hp * wp;
        int arrayHeight = bitmap.getHeight();
        int arrayWidth = bitmap.getWidth();
        Logger.logOCV("findBoundaryDots > searchLength = " + searchLength + ", bitmap w,h = " + arrayWidth + "," + arrayHeight);

        boolean isPartValid = false;
        int x = 0, y = 0;

        for (int ii = 0; ii < searchLength; ii += 3 * sizeFactor) {
            for (int jj = ii; jj > -1; jj -= 3 * sizeFactor) {
                x = jj;
                y = ii - jj;
                if (isColorDark(bitmap.getPixel(x, y))) {
                    if ((getDarknessBoubleCount(bitmap, hp, wp, y, x) > 0.6 * ap)) {
                        Point point = getCentreToMaxMatrix(bitmap, hp, y, x, false);
                        dotData.setTopLeft(point.x, point.y);
                        ii = searchLength + 1;
                        isPartValid = true;
                        break;
                    }
                }
            }
            if (!isPartValid && (ii > searchLength)) {
                Logger.logOCV("findBoundaryDots > error1");
                //   primaryDotDS.isValid = false;
                return;
            }
        }
        isPartValid = false;
        for (int ii = 0; ii < searchLength; ii += 3 * sizeFactor) {
            for (int jj = ii; jj > -1; jj -= 3 * sizeFactor) {
                x = arrayWidth - 1 - jj;
                y = ii - jj;
                if (isColorDark(bitmap.getPixel(x, y))) {
                    if ((getDarknessBoubleCount(bitmap, hp, wp, y, x) > 0.5 * ap)) {
                        Point point = getCentreToMaxMatrix(bitmap, hp, y, x, false);
                        dotData.setTopRight(point.x, point.y);
                        ii = searchLength + 1;
                        isPartValid = true;
                        break;
                    }
                }
            }
            if (!isPartValid && (ii > searchLength)) {
                Logger.logOCV("findBoundaryDots > error2");
                //   primaryDotDS.isValid = false;
                return;
            }
        }
        isPartValid = false;
        for (int ii = 0; ii < searchLength; ii += 3 * sizeFactor) {
            for (int jj = ii; jj > -1; jj -= 3 * sizeFactor) {
                x = arrayWidth - 1 - jj;
                y = arrayHeight - 1 - ii + jj;
                if (isColorDark(bitmap.getPixel(x, y))) {
                    if ((getDarknessBoubleCount(bitmap, hp, wp, y, x) > 0.5 * ap)) {
                        Point point = getCentreToMaxMatrix(bitmap, hp, y, x, false);
                        dotData.setBottomRight(point.x, point.y);
                        ii = searchLength + 1;
                        isPartValid = true;
                        break;
                    }
                }
            }
            if (!isPartValid && (ii > searchLength)) {
                Logger.logOCV("findBoundaryDots > error3");
                //  primaryDotDS.isValid = false;
                return;
            }
        }
        isPartValid = false;
        for (int ii = 0; ii < searchLength; ii += 3 * sizeFactor) {
            for (int jj = ii; jj > -1; jj -= 3 * sizeFactor) {
                x = jj;
                y = arrayHeight - 1 - ii + jj;
                if (isColorDark(bitmap.getPixel(x, y))) {
                    if ((getDarknessBoubleCount(bitmap, hp, wp, y, x) > 0.5 * ap)) {
                        Point point = getCentreToMaxMatrix(bitmap, hp, y, x, false);
                        dotData.setBottomLeft(point.x, point.y);
                        ii = searchLength + 1;
                        isPartValid = true;
                        break;
                    }
                }
            }
            if (!isPartValid && (ii > searchLength)) {
                Logger.logOCV("findBoundaryDots > error4");
                // primaryDotDS.isValid = false;
                return;
            }
        }
    }

    public void drawDotsOnBitmap(Bitmap elementBitmap) {
        int color = Color.argb(250, 0, 250, 20);
        drawDot(elementBitmap, color, dotData.topLeft);
        drawDot(elementBitmap, color, dotData.topRight);
        drawDot(elementBitmap, color, dotData.bottomLeft);
        drawDot(elementBitmap, color, dotData.bottomRight);
    }

    public void drawLinesOnMat(Mat baseMat) {
        if (dotData.getTopLine() != null)
            Imgproc.line(baseMat, dotData.getTopLeftPoint(), dotData.getTopRightPoint(), new Scalar(255, 0, 0), 4);
        if (dotData.getRightLine() != null)
            Imgproc.line(baseMat, dotData.getTopRightPoint(), dotData.getBottomRightPoint(), new Scalar(0, 255, 0), 4);
        if (dotData.getBottomLine() != null)
            Imgproc.line(baseMat, dotData.getBottomLeftPoint(), dotData.getBottomRightPoint(), new Scalar(0, 0, 255), 4);
        if (dotData.getLeftLine() != null)
            Imgproc.line(baseMat, dotData.getTopLeftPoint(), dotData.getBottomLeftPoint(), new Scalar(240, 240, 30), 4);
    }
}
