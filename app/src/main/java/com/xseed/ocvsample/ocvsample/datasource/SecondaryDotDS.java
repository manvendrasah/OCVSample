package com.xseed.ocvsample.ocvsample.datasource;

import com.xseed.ocvsample.ocvsample.pojo.Dot;
import com.xseed.ocvsample.ocvsample.pojo.Line;

/**
 * Created by Manvendra Sah on 28/03/18.
 */

public class SecondaryDotDS {

    /*  Naming convention
        tlLeft means top line left dot
        rlMid means right line mid dot
        */
    public Dot tlLeft, tlMid, tlRight, rlTop, rlMid, rlBottom, blRight, blMid, blLeft, llBottom, llMid, llTop;
    /*
        secondary lines between identity dots
    */
    private Line topLine, leftLine, rightLine, bottomLine, midLine;

    public void setTopLineLeftDot(int x, int y) {
        tlLeft = new Dot(x, y);
    }

    public void setTopLineMidDot(int x, int y) {
        tlMid = new Dot(x, y);
    }

    public void setTopLineRightDot(int x, int y) {
        tlRight = new Dot(x, y);
    }

    public void setRightLineTopDot(int x, int y) {
        rlTop = new Dot(x, y);
    }

    public void setRightLineMidDot(int x, int y) {
        rlMid = new Dot(x, y);
    }

    public void setRightLineBottomDot(int x, int y) {
        rlBottom = new Dot(x, y);
    }

    public void setBottomLineLeftDot(int x, int y) {
        blLeft = new Dot(x, y);
    }

    public void setBottomLineMidDot(int x, int y) {
        blMid = new Dot(x, y);
    }

    public void setBottomLineRightDot(int x, int y) {
        blRight = new Dot(x, y);
    }

    public void setLeftLineTopDot(int x, int y) {
        llTop = new Dot(x, y);
    }

    public void setLeftLineMidDot(int x, int y) {
        llMid = new Dot(x, y);
    }

    public void setLeftLineBottomDot(int x, int y) {
        llBottom = new Dot(x, y);
    }

    public Line getTopLine() {
        if (topLine == null) {
            topLine = new Line(llTop.x, llTop.y, rlTop.x, rlTop.y);
        }
        return topLine;
    }

    public Line getLeftLine() {
        if (leftLine == null) {
            leftLine = new Line(tlLeft.x, tlLeft.y, blLeft.x, blLeft.y);
        }
        return leftLine;
    }

    public Line getRightLine() {
        if (rightLine == null) {
            rightLine = new Line(tlRight.x, tlRight.y, blRight.x, blRight.y);
        }
        return rightLine;
    }

    public Line getMidLine() {
        if (midLine == null) {
            midLine = new Line(llMid.x, llMid.y, rlMid.x, rlMid.y);
        }
        return midLine;
    }

    public Line getBottomLine() {
        if (bottomLine == null) {
            bottomLine = new Line(llBottom.x, llBottom.y, rlBottom.x, rlBottom.y);
        }
        return bottomLine;
    }

    @Override
    public String toString() {
        return "SecondaryDotDS{" +
                "tlLeft=" + tlLeft +
                ", tlMid=" + tlMid +
                ", tlRight=" + tlRight + "\n" +
                ", rlTop=" + rlTop +
                ", rlMid=" + rlMid +
                ", rlBottom=" + rlBottom + "\n" +
                ", blRight=" + blRight +
                ", blMid=" + blMid +
                ", blLeft=" + blLeft + "\n" +
                ", llBottom=" + llBottom +
                ", llMid=" + llMid +
                ", llTop=" + llTop +
                '}';
    }

    public boolean isValid() {
        return tlLeft != null && tlMid != null && tlRight != null &&
                llTop != null && llMid != null && llBottom != null &&
                blLeft != null && blMid != null && blRight != null &&
                rlTop != null && rlMid != null && rlBottom != null;
    }
}
