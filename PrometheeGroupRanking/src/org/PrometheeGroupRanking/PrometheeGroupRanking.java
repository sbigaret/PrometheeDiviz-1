package org.PrometheeGroupRanking;

import org.PrometheeGroupRanking.xmcda.InputsHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometheeGroupRanking {
    public static List<Map<String, Double>> calculatePrometheeGroupRanking(InputsHandler.Inputs inputs) {
        List<Map<String, Double>> flows = inputs.flows;
        List<Map<String, Double>> weights = inputs.weights;
        List<Map<String, Double>> ranking = new ArrayList<>();

        for(Map<String, Double> singleWeight : weights) {

            if(singleWeight.size() != 0) {
                Map<String, Double> singleFlow = flows.get(weights.indexOf(singleWeight));
                Map<String, Double> weightedFlows = new HashMap<>();
                for (String alternative : inputs.alternatives_ids) {
                    double value = Format(singleFlow.get(alternative) * singleWeight.get(alternative));
                    weightedFlows.put(alternative, value);
                }
                ranking.add(weightedFlows);
            }
        }
        return ranking;
    }

    private static double Format(double number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
