package com.xseed.ocvsample.ocvsample.utility;

/**
 * Created by Manvendra Sah on 16/09/17.
 */

public class ErrorType {

    public static final int TYPE0 = 0;
    public static final String ERROR_0 = "No error";
    public static final int TYPE1 = 1;
    public static final String ERROR_1 = "Dots not detected";// camera is placed far from sheet, beyond threshhold
    public static final int TYPE2 = 2;
    public static final String ERROR_2 = "Boundary Line ratios beyond threshhold";
    public static final int TYPE3 = 3;
    public static final String ERROR_3 = "Detected circles below threshhold";
    public static final int TYPE4 = 4;
    public static final String ERROR_4 = "Less than two circles in an answer row"; // row line cant be obtained for extrapolation
    public static final int TYPE5 = 5;
    public static final String ERROR_5 = "Less than two left or right most answer circles";// column line cant be obtained for extrapolation
    public static final int TYPE6 = 6;
    public static final String ERROR_6 = "No consecutive answer circles";// avgccd cant be obtained
    public static final int TYPE7 = 7;
    public static final String ERROR_7 = "Less than two safe indices in answers"; // one or fewer rows are detected correctly -> cant extrapolate other rows
    public static final int TYPE8 = 8;
    public static final String ERROR_8 = "More than 2 Columns have single entry in id or grade"; // more than 2 columns have single circles detected in id or grade
    public static final int TYPE9 = 9;
    public static final String ERROR_9 = "Identity dots not detected"; // could not detect identity dots
    public static final int TYPE10 = 10;
    public static final String ERROR_10 = "Answer row not detected"; // circles in a whole answer row are found missing

    public static String[] errors = {ERROR_0, ERROR_1, ERROR_2, ERROR_3, ERROR_4, ERROR_5, ERROR_6, ERROR_7, ERROR_8, ERROR_9, ERROR_10};

    public static String getErrorString(int errorType) {
        return errors[errorType];
    }
}
