package com.xseed.ocvsample.ocvsample.scanbase.comparator;

import com.xseed.ocvsample.ocvsample.scanbase.pojo.Circle;

import java.util.Comparator;

/**
 * Created by Manvendra Sah on 07/09/17.
 */

public class AnswerCircleColumnComparator implements Comparator<Circle> {

    // for sorting answer circles by descending values of y
    @Override
    public int compare(Circle c1, Circle c2) {
        if (c1.center.y < c2.center.y)
            return 1;
        else if (c1.center.y == c2.center.y)
            return 0;
        else return -1;
    }
}
