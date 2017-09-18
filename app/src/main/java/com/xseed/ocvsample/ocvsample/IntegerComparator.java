package com.xseed.ocvsample.ocvsample;

import java.util.Comparator;

/**
 * Created by Manvendra Sah on 06/09/17.
 */

class IntegerComparator implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {
        if (o1 < o2)
            return -1;
        else if (o1 == o2)
            return 0;
        else return 1;
    }
}
