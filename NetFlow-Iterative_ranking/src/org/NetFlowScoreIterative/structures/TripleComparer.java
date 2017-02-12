package org.NetFlowScoreIterative.structures;

import java.util.Comparator;

public class TripleComparer implements Comparator<Triple> {
    public int compare(Triple x, Triple y) {
        double secX = (Double) x.second;
        double secY = (Double) y.second;

        double thirdX = (Double) x.third;
        double thirdY = (Double) y.third;

        if(secX < secY) {
            return 1;
        }
        else if(secX > secY) {
            return -1;
        }
        else {
            if((thirdX > -1.5) && (thirdY > -1.5)) {
                if(thirdX < thirdY) {
                    return 1;
                }
                else if(thirdX > thirdY) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
            else
                return 0;
        }
    }
}
