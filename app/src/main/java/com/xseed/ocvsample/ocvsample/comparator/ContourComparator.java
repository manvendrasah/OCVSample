package com.xseed.ocvsample.ocvsample.comparator;

import com.xseed.ocvsample.ocvsample.pojo.Contour;

import java.util.Comparator;

/**
 * Created by Manvendra Sah on 21/08/17.
 */

public class ContourComparator implements Comparator<Contour> {

    boolean isReverse = false;

    public ContourComparator(boolean isReverse) {
        this.isReverse = isReverse;
    }

    @Override
    public int compare(Contour o1, Contour o2) {
        if (o1.area > o2.area)
            return isReverse ? 1 : -1;
        else if (o1.area == o2.area)
            return 0;
        else return isReverse ? -1 : 1;
    }
}
