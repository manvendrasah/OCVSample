package com.xseed.ocvsample.ocvsample;

/**
 * Created by Manvendra Sah on 02/09/17.
 */

public class SheetConstants {

    public static final int SEARCH_LENGTH_DIVISOR = 4;
    public static final double LINE_RATIO_THRESHHOLD = 0.20d;

    public static final double FILTER_RADIUS_MULTIPLIER_MAX = 1.2d;
    public static final double FILTER_RADIUS_MULTIPLIER_MIN = 0.8d;

    public static final int MIN_DETECTED_CIRCLES = 50;//85
    public static final int NUM_ROWS_ANSWERS = 9;
    public static final int NUM_COLUMNS_ID = 3;
    public static final int NUM_COLUMNS_GRADE = 2;
    public static final int NUM_ANSWERS_IN_ROW = 16;
    public static final int NUM_ANSWERS_IN_COLUMN_IN_ROW = 4;
    public static final int NUM_ANSWERS_IN_ROW0 = 12;
    public static final int NUM_TOTAL_DETECTED_CIRCLES_IN_ROW = 19;
    public static final int NUM_IDS_IN_COLUMN = 10;
    public static final int NUM_GRADES_IN_COLUMN = 9;

    public static final double THRESHHOLD_CIRCLE_CENTER_DISTANCE_VERTICAL_ID0 = 4.25d;
    public static final double THRESHHOLD_CIRCLE_CENTER_DISTANCE_VERTICAL_ID1 = 7.0d;
    public static final double THRESHHOLD_CIRCLE_CENTER_DISTANCE_VERTICAL_ID2 = 10d;

    //    public static final double MULTIPLIER_THRESH_TOP0 = 7.5d;
    public static final double MULTIPLIER_CCD_THRESH_TOP0 = 2d;

    public static final double MULTIPLIER_THRESH_LEFT0 = 8;
    public static final double MULTIPLIER_THRESH_LEFT1 = 11;
    public static final double MULTIPLIER_THRESH_LEFT2 = 15;
    public static final double MULTIPLIER_THRESH_LEFT3 = 20;

    public static final double MULTIPLIER_THRESH_RIGHT0 = 5.5;
    public static final double MULTIPLIER_THRESH_RIGHT1 = 7.5;
    public static final double MULTIPLIER_THRESH_RIGHT2 = 10;
    public static final double MULTIPLIER_THRESH_RIGHT3 = 13;

    public static final double THRESHHOLD_PERPENDICULAR_VERTICAL = 1.5d;
    public static final double THRESHHOLD_PERPENDICULAR_HORIZONTAL_ID = 1.5d;
    public static final double THRESHHOLD_PERPENDICULAR_HORIZONTAL_GRADE = 2.5d;

    public static final double RATIO_DIST_BTW_CIRCLES_HOR = 1.127;
    public static final double RATIO_DIST_BTW_CIRCLES_VER = 2.582;
    public static final double RATIO_WIDTH_OUTER_BORDER = 1.164;
    public static final double RATIO_CIRCLE_DIST_FROM_COLUMN_LEFT = 3.0545;
    public static final double RATIO_CIRCLE_DIST_FROM_COLUMN_BOTTOM = 2.328;
    public static final double RATIO_ANSWER_BLOCK_WIDTH = 15.1;
    public static final double RATIO_ANSWER_BLOCK_HEIGHT = 15.2;
    public static final double RATIO_CORNER_SQUARE_SIDE = 1.382;

    public static final double DARKNESSFILTER = 0.5;
    public static final double SIZEFACTOR = 1;
    public static final double MIN_DARKNESS = 70d;//115d;

    public static final int TYPE_ANSWER = 0;
    public static final int TYPE_ID = 1;
    public static final int TYPE_GRADE = 2;

}
