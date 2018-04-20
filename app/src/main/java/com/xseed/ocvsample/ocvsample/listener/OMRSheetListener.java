package com.xseed.ocvsample.ocvsample.listener;


import android.graphics.Bitmap;

import com.xseed.ocvsample.ocvsample.pojo.Output;

import org.opencv.core.Mat;

/**
 * Created by Manvendra Sah on 22/06/17.
 */

public interface OMRSheetListener {

    void onOMRSheetGradingComplete(Bitmap originalBitmap, Mat originalMat, Output finalOutput);

    void onOMRSheetGradingFailed(int errorType);

    void onOMRSheetBitmap(Bitmap bitmap, String name);
}
