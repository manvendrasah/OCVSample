package com.xseed.ocvsample.ocvsample.scanbase.helper.dot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.graphics.ColorUtils;

import com.xseed.ocvsample.ocvsample.scanbase.pojo.Dot;
import com.xseed.ocvsample.ocvsample.scanbase.utility.SheetConstants;

import org.opencv.core.Point;

/**
 * Created by Manvendra Sah on 28/03/18.
 */

public abstract class BaseDotHelper {

    protected double getDarknessBoubleCount(Bitmap bitmap, int hp, int wp, int h, int w) {
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

    /**
     * get center co-ordinate of the dark circle
     *
     * @param lmax
     * @param h
     * @param w
     * @param re
     * @return
     */
    protected Point getCentreToMaxMatrix(final Bitmap bitmap, int lmax, int h, int w, boolean re) {
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
            double initvalue = getDarknessBoubleCount(bitmap, searchsize, searchsize, ch, cw);
            if (getDarknessBoubleCount(bitmap, searchsize, searchsize, ch + 1, cw) < getDarknessBoubleCount(
                    bitmap, searchsize, searchsize, ch - 1, cw)) {
                _h = ch - 1;
            } else {
                _h = ch + 1;
            }
            if (getDarknessBoubleCount(bitmap, searchsize, searchsize, ch, cw - 1) > getDarknessBoubleCount(bitmap,
                    searchsize, searchsize, ch, cw + 1)) {
                _w = cw - 1;
            } else {
                _w = cw + 1;
            }
            if (getDarknessBoubleCount(bitmap, searchsize, searchsize, _h, _w) <= initvalue) {
                result.x = cw;
                result.y = ch;
                if (re) {
                    return result;
                } else {
                    return getCentreToMaxMatrix(bitmap, lmax, ch, cw, true);
                }
            } else {
                ch = _h;
                cw = _w;
            }
        }
    }

    protected boolean isColorDark(int color) {
        // double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        // return darkness>=.5d;
        return ColorUtils.calculateLuminance(color) <= SheetConstants.DARKNESS_THRESHHOLD;
    }

    protected void drawDot(Bitmap elementBitmap, int color, Dot dot) {
        if (dot == null)
            return;
        int length = 6;
        Canvas canvas = new Canvas(elementBitmap);
        canvas.drawCircle(dot.x, dot.y, length, getPaint(color));
    }

    private Paint getPaint(int color) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setAntiAlias(true);
        p.setColor(color);
        return p;
    }

}
