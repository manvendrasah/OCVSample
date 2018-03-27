package com.xseed.ocvsample.ocvsample.datasource;

import com.xseed.ocvsample.ocvsample.pojo.OCVCircleConfig;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.utility.SheetConstants;

import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 27/03/18.
 */

public final class ConfigDS {

    private OCVCircleConfig config1;
    private OCVCircleConfig config2;

    private ArrayList<Integer> detectedCircleCountList;

    public static final int MAX_LIST_SIZE = 4;

    static class SingletonHolder {
        static final ConfigDS INSTANCE = new ConfigDS();
    }

    public static ConfigDS getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private ConfigDS() {
        detectedCircleCountList = new ArrayList<>();
        getConfig1();
        getConfig2();
    }

    public OCVCircleConfig getConfig() {
        if (detectedCircleCountList.size() < 2) {
            Logger.logOCV("Config selected = 1");
            return getConfig1();
        } else {
            return getConfigBaseOnCirclesDetected();
        }
    }

    private OCVCircleConfig getConfigBaseOnCirclesDetected() {
        int avg = 0, sum = 0;
        for (Integer i : detectedCircleCountList)
            sum += i;
        avg = sum / detectedCircleCountList.size();
        Logger.logOCV("Config AVG circles detected = " + avg);
        if (avg < SheetConstants.LOW_THRESHHOLD_DETECTED_CIRCLES) {
            Logger.logOCV("Config selected = 2");
            return getConfig2();
        } else {
            Logger.logOCV("Config selected = 1");
            return getConfig1();
        }
    }

    public synchronized void setCirclesDetected(int numCirclesDetected) {
        Logger.logOCV("Config circles detected = " + numCirclesDetected);
        detectedCircleCountList.add(0, numCirclesDetected);
        int size = detectedCircleCountList.size();
        if (size > MAX_LIST_SIZE) {
            for (int i = MAX_LIST_SIZE; i < size; ++i)
                detectedCircleCountList.remove(i);
        }
    }

    private OCVCircleConfig getConfig1() {
        if (config1 == null) {
            config1 = new OCVCircleConfig();
            config1.dp = 1.4d;
            config1.minDist = 25;
            config1.minRadius = 7;
            config1.maxRadius = 20;
            config1.param1 = 12;
            config1.param2 = 35;//38;
            config1.topLeftX = 0;
            config1.topLeftY = 0;
        }
        return config1;
    }

    private OCVCircleConfig getConfig2() {
        if (config2 == null) {
            config2 = new OCVCircleConfig();
            config2.dp = 1.0d;
            config2.minDist = 25;
            config2.minRadius = 7;
            config2.maxRadius = 20;
            config2.param1 = 12;
            config2.param2 = 28;
            config2.topLeftX = 0;
            config2.topLeftY = 0;
        }
        return config2;
    }
}
