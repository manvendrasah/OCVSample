package com.xseed.ocvsample.ocvsample.scanbase.listener;


import android.graphics.Bitmap;

import com.xseed.ocvsample.ocvsample.scanbase.pojo.SheetOutput;

import org.opencv.core.Mat;

/**
 * Created by Manvendra Sah on 22/06/17.
 */

public interface OMRSheetListener {

    void onOMRSheetGradingComplete(Bitmap originalBitmap, Mat originalMat, SheetOutput finalOutput);

    void onOMRSheetGradingFailed(int errorType);

    void onOMRSheetBitmap(Bitmap bitmap, String name);
}
