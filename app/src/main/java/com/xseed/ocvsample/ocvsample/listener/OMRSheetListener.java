package com.xseed.ocvsample.ocvsample.listener;


import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 22/06/17.
 */

public interface OMRSheetListener {

    void onOMRSheetGradingComplete(/*Bitmap bitmap*/);

    void onOMRSheetGradingFailed(int errorType);

    void onOMRSheetBitmap(Bitmap bitmap, String name);
}
