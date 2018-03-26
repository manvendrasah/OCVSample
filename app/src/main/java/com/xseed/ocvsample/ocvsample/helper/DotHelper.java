package com.xseed.ocvsample.ocvsample.helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.graphics.ColorUtils;

import com.xseed.ocvsample.ocvsample.datasource.DotDS;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.utility.SheetConstants;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Manvendra Sah on 05/09/17.
 */

public class DotHelper {

    private DotDS dotData;
    private Bitmap bitmap;
    private int searchLength;

    public DotDS findDots(Bitmap bitmap) {
        dotData = new DotDS();
        this.bitmap = bitmap;
        this.searchLength = bitmap.getWidth() / 3;
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
                    if ((getDarknessBoubleCount(hp, wp, y, x) > 0.6 * ap)) {
                        Point point = getCentreToMaxMatrix(hp, y, x, false);
                        dotData.setTopLeft(point.x, point.y);
                        ii = searchLength + 1;
                        isPartValid = true;
                        break;
                    }
                }
            }
            if (!isPartValid && (ii > searchLength)) {
                Logger.logOCV("findBoundaryDots > error1");
                //   dotData.isValid = false;
                return;
            }
        }
        isPartValid = false;
        for (int ii = 0; ii < searchLength; ii += 3 * sizeFactor) {
            for (int jj = ii; jj > -1; jj -= 3 * sizeFactor) {
                x = arrayWidth - 1 - jj;
                y = ii - jj;
                if (isColorDark(bitmap.getPixel(x, y))) {
                    if ((getDarknessBoubleCount(hp, wp, y, x) > 0.5 * ap)) {
                        Point point = getCentreToMaxMatrix(hp, y, x, false);
                        dotData.setTopRight(point.x, point.y);
                        ii = searchLength + 1;
                        isPartValid = true;
                        break;
                    }
                }
            }
            if (!isPartValid && (ii > searchLength)) {
                Logger.logOCV("findBoundaryDots > error2");
                //   dotData.isValid = false;
                return;
            }
        }
        isPartValid = false;
        for (int ii = 0; ii < searchLength; ii += 3 * sizeFactor) {
            for (int jj = ii; jj > -1; jj -= 3 * sizeFactor) {
                x = arrayWidth - 1 - jj;
                y = arrayHeight - 1 - ii + jj;
                if (isColorDark(bitmap.getPixel(x, y))) {
                    if ((getDarknessBoubleCount(hp, wp, y, x) > 0.5 * ap)) {
                        Point point = getCentreToMaxMatrix(hp, y, x, false);
                        dotData.setBottomRight(point.x, point.y);
                        ii = searchLength + 1;
                        isPartValid = true;
                        break;
                    }
                }
            }
            if (!isPartValid && (ii > searchLength)) {
                Logger.logOCV("findBoundaryDots > error3");
                //  dotData.isValid = false;
                return;
            }
        }
        isPartValid = false;
        for (int ii = 0; ii < searchLength; ii += 3 * sizeFactor) {
            for (int jj = ii; jj > -1; jj -= 3 * sizeFactor) {
                x = jj;
                y = arrayHeight - 1 - ii + jj;
                if (isColorDark(bitmap.getPixel(x, y))) {
                    if ((getDarknessBoubleCount(hp, wp, y, x) > 0.5 * ap)) {
                        Point point = getCentreToMaxMatrix(hp, y, x, false);
                        dotData.setBottomLeft(point.x, point.y);
                        ii = searchLength + 1;
                        isPartValid = true;
                        break;
                    }
                }
            }
            if (!isPartValid && (ii > searchLength)) {
                Logger.logOCV("findBoundaryDots > error4");
                // dotData.isValid = false;
                return;
            }
        }
    }

    /**
     * get center co-ordinate of the dark circle
     *
     * @param lmax
     * @param h
     * @param w
     * @param re
     * @return
     */
    public Point getCentreToMaxMatrix(int lmax, int h, int w, boolean re) {
        Point result = new Point();
        int ch = h;
        int cw = w;
        int pixelR = 0;
        int pixelL = 0;
        int pixelU = 0;
        int pixelD = 0;
        for (int ii = 0; ii < lmax; ii++) {
            if ((ch + ii + 2) > bitmap.getHeight()) {
                break;
            }
            if (!isColorDark(bitmap.getPixel(cw, ch + ii))) {
                //TODO yy +1
                if (!isColorDark(bitmap.getPixel(cw, ch + ii + 1))) {
                    break;
                }
            }
            pixelD = ii;
        }

        for (int ii = 0; ii < lmax; ii++) {
            if ((ch - ii - 2) < 0) {
                break;
            }
            if (!isColorDark(bitmap.getPixel(cw, ch - ii))) {
                if (!isColorDark(bitmap.getPixel(cw, ch - ii - 1))) {
                    break;
                }
            }
            pixelU = ii;
        }
        for (int ii = 0; ii < lmax; ii++) {
            if ((cw - ii - 2) < 0) {
                break;
            }
            if (!isColorDark(bitmap.getPixel(cw - ii, ch))) {
                if (!isColorDark(bitmap.getPixel(cw - ii - 1, ch))) {
                    break;
                }
            }
            pixelL = ii;
        }
        for (int ii = 0; ii < lmax; ii++) {
            //TODO yy always +1 or +2 extra
            if ((cw + ii + 2) > bitmap.getWidth()) {
                break;
            }
            if (!isColorDark(bitmap.getPixel(cw + ii, ch))) {
                if (!isColorDark(bitmap.getPixel(cw + ii + 1, ch))) {
                    break;
                }
            }
            pixelR = ii;
        }
        // found the left,right, top,bottom points
        ch += (pixelD - pixelU) / 2;
        cw += (pixelR - pixelL) / 2;
        // found the center point

        int searchsize = Math.max(pixelD + pixelU + 1, pixelR + pixelL + 1);
        // doing?? what
        while (true) {
            int _h = 0;
            int _w = 0;
            double initvalue = getDarknessBoubleCount(searchsize, searchsize, ch, cw);
            if (getDarknessBoubleCount(searchsize, searchsize, ch + 1, cw) < getDarknessBoubleCount(
                    searchsize, searchsize, ch - 1, cw)) {
                _h = ch - 1;
            } else {
                _h = ch + 1;
            }
            if (getDarknessBoubleCount(searchsize, searchsize, ch, cw - 1) > getDarknessBoubleCount(
                    searchsize, searchsize, ch, cw + 1)) {
                _w = cw - 1;
            } else {
                _w = cw + 1;
            }
            if (getDarknessBoubleCount(searchsize, searchsize, _h, _w) <= initvalue) {
                result.x = cw;
                result.y = ch;
                if (re) {
                    return result;
                } else {
                    return getCentreToMaxMatrix(lmax, ch, cw, true);
                }
            } else {
                ch = _h;
                cw = _w;
            }
        }
    }

    public boolean isColorDark(int color) {
        // double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        // return darkness>=.5d;
        return ColorUtils.calculateLuminance(color) <= 0.1d;
    }

    public double getDarknessBoubleCount(int hp, int wp, int h, int w) {
        double result = 0;
        if ((h < 0) | (h > (bitmap.getHeight() - 2)) | (w < 0) | (w > (bitmap.getWidth() - 2))) {
            return result;
        }
        int starth = Math.max((h - hp / 2), 1);
        int endh = Math.min((h + hp / 2), bitmap.getHeight() - 2);
        int startw = Math.max((w - wp / 2), 1);
        int endw = Math.min((w + wp / 2), bitmap.getWidth() - 2);
        for (int hh = starth; hh < endh; hh++) {
            for (int ww = startw; ww < endw; ww++) {
                if (isColorDark(bitmap.getPixel(ww, hh))) {
                    result += 1.0;
                }
            }
        }
        return result;
    }

    public void drawDotsOnBitmap(Bitmap finalBitmap) {
        int color = Color.argb(250, 0, 250, 20);
        int length = 6;
        Canvas canvas = new Canvas(finalBitmap);
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setAntiAlias(true);
        p.setColor(color);
        canvas.drawCircle(dotData.topLeft.x, dotData.topLeft.y, length, p);
        canvas.drawCircle(dotData.topRight.x, dotData.topRight.y, length, p);
        canvas.drawCircle(dotData.bottomLeft.x, dotData.bottomLeft.y, length, p);
        canvas.drawCircle(dotData.bottomRight.x, dotData.bottomRight.y, length, p);
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
