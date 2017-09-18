package com.xseed.ocvsample.ocvsample;

import org.opencv.core.Mat;

/**
 * Created by Manvendra Sah on 02/09/17.
 */

public class CircleRatios {

    double avgAnswerRadius = 0, avgIdGradeRadius = 0;
    double hc; // horizontal distance between circles
    double dx; // outer border w,h
    double dh; // horizontal distance of first circle from vertical column line
    double cw; // answer block width
    double cw2; // answer block width
    private DotDS dotData;
    private double ratioHorizontalLineLen = 0, ratioVerticalLineLen = 0;
    private Mat baseMat;

    public CircleRatios(DotDS dotData, Mat baseMat) {
        this.baseMat = baseMat;
        this.dotData = dotData;
        getBottomToTopLineRatio();
        getRightToLeftLineRatio();
    }

    public void setAvgAnswerRadius(double radius) {
        this.avgAnswerRadius = radius;
        hc = avgAnswerRadius * SheetConstants.RATIO_DIST_BTW_CIRCLES_HOR;
        dx = avgAnswerRadius * SheetConstants.RATIO_WIDTH_OUTER_BORDER;
        dh = avgAnswerRadius * SheetConstants.RATIO_CIRCLE_DIST_FROM_COLUMN_LEFT;
        cw = avgAnswerRadius * SheetConstants.RATIO_ANSWER_BLOCK_WIDTH;
        cw2 = 9d * avgAnswerRadius + 3d * hc + dh;
        cw = Math.ceil((cw + cw2) / 2d);
    }

    public double getBottomToTopLineRatio() {
        if (ratioHorizontalLineLen == 0) {
            double topLineLen = Utility.getLineLength(dotData.getTopLine());
            double bottomLineLen = Utility.getLineLength(dotData.getBottomLine());
            ratioHorizontalLineLen = bottomLineLen / topLineLen;
        }
        return ratioHorizontalLineLen;
    }

    public double getRightToLeftLineRatio() {
        if (ratioVerticalLineLen == 0) {
            double leftLineLen = Utility.getLineLength(dotData.getLeftLine());
            double rightLineLen = Utility.getLineLength(dotData.getRightLine());
            ratioVerticalLineLen = rightLineLen / leftLineLen;
        }
        return ratioVerticalLineLen;
    }

    public double getFilterMaxRadius() {
        return avgAnswerRadius * SheetConstants.FILTER_RADIUS_MULTIPLIER_MAX;
    }

    public double getFilterMinRadius() {
        return avgAnswerRadius * SheetConstants.FILTER_RADIUS_MULTIPLIER_MIN;
    }

    public double getVerticalThreshholdBetweenCirclesForSorting() {
        return SheetConstants.THRESHHOLD_PERPENDICULAR_VERTICAL * avgAnswerRadius;
    }

    public double getHorizontalThreshholdBetweenIds() {
        return SheetConstants.THRESHHOLD_PERPENDICULAR_HORIZONTAL_ID * avgAnswerRadius;
    }

    public double getHorizontalThreshholdForGrade() {
        return SheetConstants.THRESHHOLD_PERPENDICULAR_HORIZONTAL_GRADE * avgAnswerRadius;
    }

    public double getAnswerCirclesCenterDistanceThreshhold() {
        return 3d * avgAnswerRadius + hc;
    }

    public double getAnswerCirclesLeftPerpendicularThreshhold(int index) {
        if (index == 0)
            return avgAnswerRadius * SheetConstants.MULTIPLIER_THRESH_LEFT0;
        else if (index == 1)
            return avgAnswerRadius * SheetConstants.MULTIPLIER_THRESH_LEFT1;
        else
            throw new RuntimeException("Left perpendicular threshhold must be used only for circles at index 0 and 1");
    }

    public double getAnswerCirclesRightPerpendicularThreshhold(int index) {
        if (index == 0)
            return avgAnswerRadius * SheetConstants.MULTIPLIER_THRESH_RIGHT0;
        else if (index == 1)
            return avgAnswerRadius * SheetConstants.MULTIPLIER_THRESH_RIGHT1;
        else
            throw new RuntimeException("Right perpendicular threshhold must be used only for circles at index 0 and 1");
    }

    public double getVerticalThreshholdBetweenIdCircles(int index) {
        if (index == 0)
            return avgIdGradeRadius * SheetConstants.THRESHHOLD_CIRCLE_CENTER_DISTANCE_VERTICAL_ID0;
        else if (index == 1)
            return avgIdGradeRadius * SheetConstants.THRESHHOLD_CIRCLE_CENTER_DISTANCE_VERTICAL_ID1;
        else if (index == 2)
            return avgIdGradeRadius * SheetConstants.THRESHHOLD_CIRCLE_CENTER_DISTANCE_VERTICAL_ID2;
        else
            throw new RuntimeException("Vertical Threshhold between id circles can be used upto 2 levels only");
    }

    public double getTopPerpThreshholdForIdCircles() {
        return avgIdGradeRadius * SheetConstants.MULTIPLIER_CCD_THRESH_TOP0;
    }

    @Override
    public String toString() {
        return "CircleRatios{" +
                "avgAnswerRadius=" + avgAnswerRadius +
                ", avgIdGradeRadius=" + avgIdGradeRadius +
                ", ratioHorizontalLineLen=" + ratioHorizontalLineLen +
                ", ratioVerticalLineLen=" + ratioVerticalLineLen +
                ", hc=" + hc +
                ", dx=" + dx +
                ", dh=" + dh +
                ", cw=" + cw +
                ", cw2=" + cw2 +
                '}';
    }

    public double getIdGradePerpThreshholdToAnsLine() {
        return avgIdGradeRadius * 1.5d;
    }

    public boolean areValidLineRatios() {
        double dRatioVert = Math.abs(1 - ratioVerticalLineLen);
        double dRatioHorz = Math.abs(1 - ratioHorizontalLineLen);
        return dRatioHorz < SheetConstants.LINE_RATIO_THRESHHOLD && dRatioVert < SheetConstants.LINE_RATIO_THRESHHOLD;
    }
}
