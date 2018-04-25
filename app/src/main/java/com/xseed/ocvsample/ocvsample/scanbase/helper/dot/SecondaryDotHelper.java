package com.xseed.ocvsample.ocvsample.scanbase.helper.dot;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.xseed.ocvsample.ocvsample.scanbase.datasource.PrimaryDotDS;
import com.xseed.ocvsample.ocvsample.scanbase.datasource.SecondaryDotDS;
import com.xseed.ocvsample.ocvsample.scanbase.pojo.Dot;
import com.xseed.ocvsample.ocvsample.scanbase.utility.Logger;
import com.xseed.ocvsample.ocvsample.scanbase.utility.SheetConstants;
import com.xseed.ocvsample.ocvsample.scanbase.utility.Utility;

import org.opencv.core.Point;

import java.util.HashSet;

/**
 * Created by Manvendra Sah on 28/03/18.
 */

public class SecondaryDotHelper extends BaseDotHelper {
    private PrimaryDotDS primaryDotData;
    private SecondaryDotDS thDotData; // theoretical dot data
    private SecondaryDotDS calcDotData; // calculated dot data
    private Bitmap bitmap;
    private int loopVar, hp, wp;
    private double ap;
    private int bitmapWidth, bitmapHeight;
    private HashSet<String> setSquarePixel;

    public SecondaryDotHelper(PrimaryDotDS primaryDotData, Bitmap bitmap) {
        this.primaryDotData = primaryDotData;
        this.bitmap = bitmap;
        setConstants();
        calcDotData = new SecondaryDotDS();
    }

    public SecondaryDotDS getTheoreticalIdentityDots() {
        return thDotData;
    }

    public SecondaryDotDS getCalculatedIdentityDots() {
        return calcDotData;
    }

    public void setTheoreticalIdentityDots() {
        thDotData = new SecondaryDotDS();
        Dot topLeft = primaryDotData.topLeft;
        Dot topRight = primaryDotData.topRight;
        Dot bottomLeft = primaryDotData.bottomLeft;
        Dot bottomRight = primaryDotData.bottomRight;
        thDotData.tlLeft = Utility.getDotBetweenDots(topLeft, topRight, SheetConstants.DIST_TLLEFT, SheetConstants.DIST_LEFT_RIGHT);
        thDotData.tlMid = Utility.getDotBetweenDots(topLeft, topRight, SheetConstants.DIST_TLMID, SheetConstants.DIST_LEFT_RIGHT);
        thDotData.tlRight = Utility.getDotBetweenDots(topRight, topLeft, SheetConstants.DIST_TLRIGHT, SheetConstants.DIST_LEFT_RIGHT);

        thDotData.rlTop = Utility.getDotBetweenDots(topRight, bottomRight, SheetConstants.DIST_LLTOP, SheetConstants.DIST_TOP_BOTTOM);
        thDotData.rlMid = Utility.getDotBetweenDots(topRight, bottomRight, SheetConstants.DIST_LLMID, SheetConstants.DIST_TOP_BOTTOM);
        thDotData.rlBottom = Utility.getDotBetweenDots(bottomRight, topRight, SheetConstants.DIST_LLBOTTOM, SheetConstants.DIST_TOP_BOTTOM);

        thDotData.blLeft = Utility.getDotBetweenDots(bottomLeft, bottomRight, SheetConstants.DIST_TLLEFT, SheetConstants.DIST_LEFT_RIGHT);
        thDotData.blMid = Utility.getDotBetweenDots(bottomLeft, bottomRight, SheetConstants.DIST_TLMID, SheetConstants.DIST_LEFT_RIGHT);
        thDotData.blRight = Utility.getDotBetweenDots(bottomRight, bottomLeft, SheetConstants.DIST_TLRIGHT, SheetConstants.DIST_LEFT_RIGHT);

        thDotData.llTop = Utility.getDotBetweenDots(topLeft, bottomLeft, SheetConstants.DIST_LLTOP, SheetConstants.DIST_TOP_BOTTOM);
        thDotData.llMid = Utility.getDotBetweenDots(topLeft, bottomLeft, SheetConstants.DIST_LLMID, SheetConstants.DIST_TOP_BOTTOM);
        thDotData.llBottom = Utility.getDotBetweenDots(bottomLeft, topLeft, SheetConstants.DIST_LLBOTTOM, SheetConstants.DIST_TOP_BOTTOM);
    }

    public void searchForVerticalDots() {
        Logger.logOCV("#===RIGHT LINE TOP===# \nDot > thDot > " + thDotData.rlTop.x + "," + thDotData.rlTop.y);
        calcDotData.rlTop = findDotNearBoundary(thDotData.rlTop);
        Logger.logOCV("#===RIGHT LINE MID===# \nDot > thDot > " + thDotData.rlMid.x + "," + thDotData.rlMid.y);
        calcDotData.rlMid = findDotNearVerticalMiddle(thDotData.rlMid);
        Logger.logOCV("#===RIGHT LINE BOTTOM===# \nDot > thDot > " + thDotData.rlBottom.x + "," + thDotData.rlBottom.y);
        calcDotData.rlBottom = findDotNearBoundary(thDotData.rlBottom);

        Logger.logOCV("#===LEFT LINE TOP===# \nDot > thDot > " + thDotData.llTop.x + "," + thDotData.llTop.y);
        calcDotData.llTop = findDotNearBoundary(thDotData.llTop);
        Logger.logOCV("#===LEFT LINE MID===# \nDot > thDot > " + thDotData.llMid.x + "," + thDotData.llMid.y);
        calcDotData.llMid = findDotNearVerticalMiddle(thDotData.llMid);
        Logger.logOCV("#===LEFT LINE BOTTOM===# \nDot > thDot > " + thDotData.llBottom.x + "," + thDotData.llBottom.y);
        calcDotData.llBottom = findDotNearBoundary(thDotData.llBottom);
    }

    public void searchForHorizontalDots() {
        Logger.logOCV("#===TOP LINE LEFT===# \nDot > thDot > " + thDotData.tlLeft.x + "," + thDotData.tlLeft.y);
        calcDotData.tlLeft = findDotNearBoundary(thDotData.tlLeft);
        Logger.logOCV("#===TOP LINE MID===# \nDot > thDot > " + thDotData.tlMid.x + "," + thDotData.tlMid.y);
        calcDotData.tlMid = findDotNearHorizontalMiddle(thDotData.tlMid);
        Logger.logOCV("#===TOP LINE RIGHT===# \nDot > thDot > " + thDotData.tlRight.x + "," + thDotData.tlRight.y);
        calcDotData.tlRight = findDotNearBoundary(thDotData.tlRight);

        Logger.logOCV("#===BOTTOM LINE LEFT===# \nDot > thDot > " + thDotData.blLeft.x + "," + thDotData.blLeft.y);
        calcDotData.blLeft = findDotNearBoundary(thDotData.blLeft);
        Logger.logOCV("#===BOTTOM LINE MID===# \nDot > thDot > " + thDotData.blMid.x + "," + thDotData.blMid.y);
        calcDotData.blMid = findDotNearHorizontalMiddle(thDotData.blMid);
        Logger.logOCV("#===BOTTOM LINE RIGHT===# \nDot > thDot > " + thDotData.blRight.x + "," + thDotData.blRight.y);
        calcDotData.blRight = findDotNearBoundary(thDotData.blRight);
    }

    private void setConstants() {
        loopVar = 5 * (int) SheetConstants.SIZEFACTOR;
        hp = (int) (10 * SheetConstants.SIZEFACTOR);
        wp = (int) (10 * SheetConstants.SIZEFACTOR);
        ap = hp * wp;
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
    }

    private Dot findDotNearBoundary(Dot thDot) {
        setSquarePixel = new HashSet<>();
        Dot dot = findDot(thDot, 2, 2);
        if (dot == null)
            dot = findDot(thDot, 4, 4);
        if (dot == null)
            dot = findDot(thDot, 6, 6);
       /* if (dot == null)
            dot = findDot(thDot, 8, 8);
        if (dot == null)
            dot = findDot(thDot, 12, 12);*/

        return dot;
    }

    private Dot findDotNearHorizontalMiddle(Dot thDot) {
        setSquarePixel = new HashSet<>();
        Dot dot = findDot(thDot, 2, 2);
        if (dot == null)
            dot = findDot(thDot, 4, 4);
        if (dot == null)
            dot = findDot(thDot, 4, 6);
        if (dot == null)
            dot = findDot(thDot, 4, 8);
        /*if (dot == null)
            dot = findDot(thDot, 8, 12);
        if (dot == null)
            dot = findDot(thDot, 12, 16);*/
        return dot;
    }

    private Dot findDotNearVerticalMiddle(Dot thDot) {
        setSquarePixel = new HashSet<>();
        Dot dot = findDot(thDot, 2, 2);
        if (dot == null)
            dot = findDot(thDot, 4, 4);
        if (dot == null)
            dot = findDot(thDot, 6, 4);
        if (dot == null)
            dot = findDot(thDot, 8, 4);
       /* if (dot == null)
            dot = findDot(thDot, 12, 8);
        if (dot == null)
            dot = findDot(thDot, 16, 12);*/
        return dot;
    }

    private Dot findDot(Dot thDot, int numPixSquaresVert, int numPixSquaresHorz) {
        if (numPixSquaresVert % 2 == 1 || numPixSquaresHorz % 2 == 1)
            throw new RuntimeException("Number of Pixel Squares has to be EVEN");
        int searchLengthHorz = numPixSquaresHorz * loopVar;
        int searchLengthVert = numPixSquaresVert * loopVar;

        int x = 0, y = 0, ii = 0;
        int dx = thDot.x - searchLengthHorz / 2;
        int dy = thDot.y - searchLengthVert / 2;

        Logger.logOCV("Dot > FUNC > dx,dy = " + dx + "," + dy + "  bitmap wd, ht = " + bitmapWidth + "," + bitmapHeight);

        for (int i = 0; i < numPixSquaresVert; ++i) {
            y = dy + i * loopVar;
            for (int j = 0; j < numPixSquaresHorz; ++j) {
                x = dx + j * loopVar;
                String key = String.valueOf(x) + "," + y;
//                Logger.logOCV("Dot > Inside1 > " + x + "," + y );
                if (!setSquarePixel.contains(key) && x > 0 && y > 0 && x < bitmapWidth && y < bitmapHeight
                        && isColorDark(bitmap.getPixel(x, y))) {
                    double darkness = getDarknessBoubleCount(bitmap, hp, wp, y, x);
                    Logger.logOCV("Dot > Inside1 > " + x + "," + y + ", darkness = " + darkness);
                    if (darkness > 0.5 * ap) {
                        Point point = getCentreToMaxMatrix(bitmap, hp, y, x, false);
                        Logger.logOCV("Dot > Point > " + point.x + "," + point.y);
                        return new Dot(point.x, point.y);
                    } else {
                        Logger.logOCV("Dot > NOT DARK > " + x + "," + y + "  0.5ap = " + (0.5 * ap));
                        setSquarePixel.add(key);
                    }
                }
            }
        }

        Logger.logOCV("Dot > Point > NULL");
        return null;
    }

    private Dot findDotDiagonally(Dot thDot, int numPixSquaresVert, int numPixSquaresHorz) {
        if (numPixSquaresVert % 2 == 1 || numPixSquaresHorz % 2 == 1)
            throw new RuntimeException("Number of Pixel Squares has to be EVEN");
        int searchLengthHorz = numPixSquaresHorz * loopVar;
        int searchLengthVert = numPixSquaresVert * loopVar;

        int x = 0, y = 0, ii = 0;
        int dx = thDot.x - searchLengthHorz / 2;
        int sx = thDot.x + searchLengthHorz / 2;
        int dy = thDot.y - searchLengthVert / 2;
        int sy = thDot.y + searchLengthVert / 2;
        int breakX = sx - loopVar;
        int breakY = sy - loopVar;

        Logger.logOCV("Dot > dx,dy = " + dx + "," + dy + "  sx,sy = " + sx + "," + sy + "   breaks = " + breakX + "," + breakY);

        int op = 0;
        int maxNumSquarePixels = numPixSquaresVert > numPixSquaresHorz ? numPixSquaresVert : numPixSquaresHorz;
        int maxOps = (maxNumSquarePixels + 1) * (maxNumSquarePixels + 2) / 2 + (maxNumSquarePixels + 3) / 2;
        while (op < maxOps) {
            for (int jj = ii; jj > -1; jj -= loopVar) {
                x = dx + jj;
                y = dy + ii - jj;
                String key = String.valueOf(x) + "," + y;
                Logger.logOCV("Dot > Inside > " + x + "," + y);
                if (!setSquarePixel.contains(key) && x > 0 && y > 0 && x < bitmapWidth && y < bitmapHeight
                        && x < sx && y < sy && isColorDark(bitmap.getPixel(x, y))) {
                    if ((getDarknessBoubleCount(bitmap, hp, wp, y, x) > 0.5 * ap)) {
                        Point point = getCentreToMaxMatrix(bitmap, hp, y, x, false);
                        Logger.logOCV("Dot > Point > " + point.x + "," + point.y);
                        return new Dot(point.x, point.y);
                    } else
                        setSquarePixel.add(key);
                }
            }
            ii += loopVar;
            op++;
//            Logger.logOCV("Dot > Outside > " + x + "," + y);
        }
        Logger.logOCV("Dot > Point > NULL");
        return null;
    }

    public void drawCalculatedIdentityDots(Bitmap elementBitmap) {
        int color = Color.argb(250, 250, 20, 200);
        drawDot(elementBitmap, color, calcDotData.tlLeft);
        drawDot(elementBitmap, color, calcDotData.tlMid);
        drawDot(elementBitmap, color, calcDotData.tlRight);
        drawDot(elementBitmap, color, calcDotData.rlTop);
        drawDot(elementBitmap, color, calcDotData.rlMid);
        drawDot(elementBitmap, color, calcDotData.rlBottom);
        drawDot(elementBitmap, color, calcDotData.blLeft);
        drawDot(elementBitmap, color, calcDotData.blMid);
        drawDot(elementBitmap, color, calcDotData.blRight);
        drawDot(elementBitmap, color, calcDotData.llTop);
        drawDot(elementBitmap, color, calcDotData.llMid);
        drawDot(elementBitmap, color, calcDotData.llBottom);
    }

    public void drawTheoreticalIdentityDots(Bitmap elementBitmap) {
        int color = Color.argb(250, 250, 20, 200);
        drawDot(elementBitmap, color, thDotData.tlLeft);
        drawDot(elementBitmap, color, thDotData.tlMid);
        drawDot(elementBitmap, color, thDotData.tlRight);
        drawDot(elementBitmap, color, thDotData.rlTop);
        drawDot(elementBitmap, color, thDotData.rlMid);
        drawDot(elementBitmap, color, thDotData.rlBottom);
        drawDot(elementBitmap, color, thDotData.blLeft);
        drawDot(elementBitmap, color, thDotData.blMid);
        drawDot(elementBitmap, color, thDotData.blRight);
        drawDot(elementBitmap, color, thDotData.llTop);
        drawDot(elementBitmap, color, thDotData.llMid);
        drawDot(elementBitmap, color, thDotData.llBottom);
    }
}
