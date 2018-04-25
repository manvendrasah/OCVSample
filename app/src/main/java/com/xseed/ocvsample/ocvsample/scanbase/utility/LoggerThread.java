package com.xseed.ocvsample.ocvsample.scanbase.utility;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.xseed.ocvsample.ocvsample.scanbase.utility.Utility;

/**
 * Created by Manvendra Sah on 16/09/17.
 */

public class LoggerThread extends HandlerThread implements Handler.Callback {

    private Handler handler;

    public LoggerThread(String name) {
        super(name);
    }

    public LoggerThread(String name, int priority) {
        super(name, priority);
    }

    @Override
    protected void onLooperPrepared() {
        handler = new Handler(getLooper(), this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        String log = (String) msg.obj;
        Utility.writeToLogFile(log);
        return true;
    }

    public Handler getHandler() {
        return handler;
    }
}