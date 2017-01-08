package org.NetFlowScoreIterative.structures;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Magdalena on 2017-01-07.
 */
public class ListMapsComparer implements Comparator<Map<String, Double>> {
    public int compare(Map<String, Double> m1, Map<String, Double> m2) {
        Map.Entry<String,Double> entry = m1.entrySet().iterator().next();
        Map.Entry<String,Double> entry2 = m2.entrySet().iterator().next();
        if(entry.getValue() < entry2.getValue()) {
            return 1;
        }
        else if(entry.getValue() > entry2.getValue()) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
