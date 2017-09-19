package com.xseed.ocvsample.ocvsample.comparator;

import com.xseed.ocvsample.ocvsample.pojo.Circle;

import java.util.Comparator;

/**
 * Created by Manvendra Sah on 30/08/17.
 */

public class CirclePointComparator implements Comparator<Circle> {

    double avgRad;

    public CirclePointComparator(double avgRadius) {
        this.avgRad = avgRadius;
    }

    @Override
    public int compare(Circle c1, Circle c2) {
        int diffY = (int) (c1.center.y - c2.center.y);
        if (diffY > avgRad)
            return -1;
        else if (diffY < (-1 * avgRad))
            return 1;
        else {
            int diffX = (int) (c1.center.x - c2.center.x);
            if (diffX > avgRad)
                return 1;
            else if (diffX < (-1 * avgRad))
                return -1;
            else return 0;
        }
    }
}
