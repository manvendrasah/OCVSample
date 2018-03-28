package com.xseed.ocvsample.ocvsample.helper.dot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.xseed.ocvsample.ocvsample.datasource.PrimaryDotDS;
import com.xseed.ocvsample.ocvsample.datasource.SecondaryDotDS;
import com.xseed.ocvsample.ocvsample.pojo.Dot;
import com.xseed.ocvsample.ocvsample.utility.SheetConstants;
import com.xseed.ocvsample.ocvsample.utility.Utility;

/**
 * Created by Manvendra Sah on 28/03/18.
 */

public class SecondaryDotHelper extends BaseDotHelper {
    private PrimaryDotDS primaryDotData;
    private SecondaryDotDS thDotData; // theoretical dot data
    private SecondaryDotDS calcDotData; // calculated dot data
    private Bitmap bitmap;

    public SecondaryDotHelper(PrimaryDotDS primaryDotData) {
        this.primaryDotData = primaryDotData;
    }

    public void setTheoreticalIdentityDots() {
        thDotData = new SecondaryDotDS();
        Dot topLeft = primaryDotData.topLeft;
        Dot topRight = primaryDotData.topRight;
        Dot bottomLeft = primaryDotData.bottomLeft;
        Dot bottomRight = primaryDotData.bottomRight;
        thDotData.tlLeft = Utility.getDotBetweenDots(topLeft, topRight, SheetConstants.DIST_TLLEFT, SheetConstants.DIST_LEFT_RIGHT);
        thDotData.tlRight = Utility.getDotBetweenDots(topRight, topLeft, SheetConstants.DIST_TLRIGHT, SheetConstants.DIST_LEFT_RIGHT);

        thDotData.rlTop = Utility.getDotBetweenDots(topRight, bottomRight, SheetConstants.DIST_LLTOP, SheetConstants.DIST_TOP_BOTTOM);
        thDotData.rlMid = Utility.getDotBetweenDots(topRight, bottomRight, SheetConstants.DIST_LLMID, SheetConstants.DIST_TOP_BOTTOM);
        thDotData.rlBottom = Utility.getDotBetweenDots(bottomRight, topRight, SheetConstants.DIST_LLBOTTOM, SheetConstants.DIST_TOP_BOTTOM);

        thDotData.blLeft = Utility.getDotBetweenDots(bottomLeft, bottomRight, SheetConstants.DIST_TLLEFT, SheetConstants.DIST_LEFT_RIGHT);
        thDotData.blRight = Utility.getDotBetweenDots(bottomRight, bottomLeft, SheetConstants.DIST_TLRIGHT, SheetConstants.DIST_LEFT_RIGHT);

        thDotData.llTop = Utility.getDotBetweenDots(topLeft, bottomLeft, SheetConstants.DIST_LLTOP, SheetConstants.DIST_TOP_BOTTOM);
        thDotData.llMid = Utility.getDotBetweenDots(topLeft, bottomLeft, SheetConstants.DIST_LLMID, SheetConstants.DIST_TOP_BOTTOM);
        thDotData.llBottom = Utility.getDotBetweenDots(bottomLeft, topLeft, SheetConstants.DIST_LLBOTTOM, SheetConstants.DIST_TOP_BOTTOM);
    }

    public void drawTheoreticalIdentityDots(Bitmap elementBitmap) {
        int color = Color.argb(250, 250, 20, 200);
        int length = 6;
        Canvas canvas = new Canvas(elementBitmap);
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setAntiAlias(true);
        p.setColor(color);
        drawDot(thDotData.tlLeft, canvas, length, p);
        drawDot(thDotData.tlRight, canvas, length, p);
        drawDot(thDotData.rlTop, canvas, length, p);
        drawDot(thDotData.rlMid, canvas, length, p);
        drawDot(thDotData.rlBottom, canvas, length, p);
        drawDot(thDotData.blLeft, canvas, length, p);
        drawDot(thDotData.blRight, canvas, length, p);
        drawDot(thDotData.llTop, canvas, length, p);
        drawDot(thDotData.llMid, canvas, length, p);
        drawDot(thDotData.llBottom, canvas, length, p);
    }

    private void drawDot(Dot dot, Canvas canvas, int length, Paint p) {
        canvas.drawCircle(dot.x, dot.y, length, p);
    }
}
