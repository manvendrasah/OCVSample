package com.xseed.ocvsample.ocvsample.scanbase.utility;

/**
 * Created by Manvendra Sah on 02/09/17.
 */

public class SheetConstants {
    /*
         Sheet constants
    */
    public static final int NUM_ROWS_ANSWERS = 9;
    public static final int NUM_COLUMNS_ID = 3;
    public static final int NUM_COLUMNS_GRADE = 2;
    public static final int NUM_ANSWERS_IN_ROW = 16;
    public static final int NUM_ANSWERS_IN_COLUMN_IN_ROW = 4;
    public static final int NUM_ANSWERS_IN_ROW0 = 12;
    public static final int NUM_TOTAL_DETECTED_CIRCLES_IN_ROW = 19;
    public static final int NUM_IDS_IN_COLUMN = 10;
    public static final int NUM_GRADES_IN_COLUMN = 9;
    /*
        Circle types
    */
    public static final int TYPE_ANSWER = 0;
    public static final int TYPE_ID = 1;
    public static final int TYPE_GRADE = 2;
    /*
        Constants used in Blob detection
    */
    public static final double DARKNESS_THRESHHOLD = 0.1d;
    public static final double DARKNESS_FILTER = 0.5;
    public static final double SIZEFACTOR = 1;
    public static final double MIN_DARKNESS = 110d;//115d, 70d;
    /*
        Constants for configuring scanning constraints
    */
    public static final int SEARCH_LENGTH_DIVISOR = 3;
    public static final int MIN_DETECTED_CIRCLES = 50;//85
    public static final int MIN_DETECTED_IDGRADE_CIRCLES = 8;
    public static final int LOW_THRESHHOLD_DETECTED_CIRCLES = 110;
    public static final double LINE_RATIO_THRESHHOLD = 0.20d;
    public static final double LINE_SLOPE_THRESHHOLD = 80; //degrees
    /*
         Ratios to circle radius, calculated manually from sheet
     */
    public static final double RATIO_DIST_BTW_CIRCLES_HOR = 1.127;
    public static final double RATIO_DIST_BTW_CIRCLES_VER = 2.582;
    public static final double RATIO_WIDTH_OUTER_BORDER = 1.164;
    public static final double RATIO_CIRCLE_DIST_FROM_COLUMN_LEFT = 3.0545;
    public static final double RATIO_CIRCLE_DIST_FROM_COLUMN_BOTTOM = 2.328;
    public static final double RATIO_ANSWER_BLOCK_WIDTH = 15.1;
    public static final double RATIO_ANSWER_BLOCK_HEIGHT = 15.2;
    public static final double RATIO_CORNER_SQUARE_SIDE = 1.382;
    /*
        Min and Max ratios to avg Radius to filter extra circles
    */
    public static final double FILTER_RADIUS_MULTIPLIER_MAX = 1.2d;
    public static final double FILTER_RADIUS_MULTIPLIER_MIN = 0.8d;
    /*
        Threshhold constants wrt avg Radius between circles and lines
    */
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
    public static final double THRESHHOLD_PERPENDICULAR_HORIZONTAL_ID = 1.35d;
    public static final double THRESHHOLD_PERPENDICULAR_HORIZONTAL_GRADE = 2.5d;
    /*
        Pixel value for distances between seconday DOTS' CENTERS to their closest primary dots' centers
    */
    public static final int DIST_LEFT_RIGHT = 426;// distance between top left and top right dots
    public static final int DIST_TOP_BOTTOM = 565;// distance between top left and bottom left dots
    public static final int DIST_TLLEFT = 40; // distance of (secondary) top line left dot from top left (primary) dot
    public static final int DIST_TLMID = 213; // distance of (secondary) top line left dot from top left (primary) dot
    public static final int DIST_TLRIGHT = 26;// distance of (secondary) top line right dot from top right (primary) dot
    public static final int DIST_LLTOP = 51;// distance of (secondary) left line top dot from top left (primary) dot
    public static final int DIST_LLMID = 272;// distance of (secondary)left line mid dot from top left (primary) dot
    public static final int DIST_LLBOTTOM = 34;// distance of (secondary)left line bottom dot from bottom left (primary) dot
    /*
        Weight value for circles in different BLOCK rows
    */
    public static final int WEIGHT_DIST_ROW_CLOSER = 147;
    public static final int WEIGHT_DIST_ROW_FARTHER = 205;
}
