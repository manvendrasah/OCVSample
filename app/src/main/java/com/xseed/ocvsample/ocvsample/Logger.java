package com.xseed.ocvsample.ocvsample;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.answers.CustomEvent;

import java.util.Map;

/**
 * Created by Manvendra Sah on 24/08/17.
 */

public class Logger {
    static private Logger _instance;
    private static LoggerThread loggerThread;
    private static Handler mHandler;

    static {
        loggerThread = new LoggerThread("LoggerThread");
        loggerThread.start();
    }

    public static void logOMR(String msg) {
        logOCV(msg);
    }

    public static void logOCV(String msg) {
        Log.d("OCVSample", "OCV > " + msg);
        if (mHandler == null)
            mHandler = loggerThread.getHandler();
        if (mHandler != null) {
            Message message = new Message();
            message.obj = msg;
            mHandler.sendMessage(message);
        }
    }

    public static void logFA(String msg) {
        Log.d("OCVSample", "OCV > FA > " + msg);
    }

    public static void logEventToFA(String eventName, Map<String, Object> attributeMap) {
        if (TextUtils.isEmpty(eventName) || attributeMap == null || attributeMap.isEmpty())
            return;

        Logger.logFA(eventName + " > " + attributeMap.toString());

        CustomEvent event = new CustomEvent(eventName);
        for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (val instanceof Number) {
                Number num = (Number) val;
                event.putCustomAttribute(key, num);
            } else if (val instanceof String) {
                String str = (String) val;
                event.putCustomAttribute(key, str);
            }
        }
        event.putCustomAttribute("Android Version", android.os.Build.VERSION.SDK_INT);
        event.putCustomAttribute("Device Model", Build.MODEL);
        event.putCustomAttribute("Brand", Build.MANUFACTURER);
        //Answers.getInstance().logCustom(event);
    }

    public static void quitLoggerThread() {
        loggerThread.quit();
    }
}