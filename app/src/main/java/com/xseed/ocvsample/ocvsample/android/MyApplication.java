package com.xseed.ocvsample.ocvsample.android;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.xseed.ocvsample.ocvsample.scanbase.utility.Logger;
import com.xseed.ocvsample.ocvsample.scanbase.utility.Utility;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Manvendra Sah on 07/08/17.
 */

public class MyApplication extends Application {
    static {
        System.loadLibrary("opencv_java3");
    }

    private static MyApplication instance = null;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        instance = this;
//        Utility.deleteImageDirectory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.quitLoggerThread();
    }
}
