package com.xseed.ocvsample.ocvsample;

import java.util.Comparator;

/**
 * Created by Manvendra Sah on 02/09/17.
 */

public class AnswerCircleRowComparator implements Comparator<Circle> {

    @Override
    public int compare(Circle c1, Circle c2) {
        if (c1.center.x < c2.center.x)
            return -1;
        else if (c1.center.x == c2.center.x)
            return 0;
        else return 1;
    }
}
