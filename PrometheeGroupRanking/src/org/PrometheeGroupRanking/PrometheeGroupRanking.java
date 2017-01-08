package org.PrometheeGroupRanking;

import org.PrometheeGroupRanking.xmcda.InputsHandler;
import org.xmcda.Alternative;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PrometheeGroupRanking {
    public static Map<String, Double> calculatePrometheeGroupRanking(InputsHandler.Inputs inputs) {
        List<Map<String, Double>> flows = inputs.flows;
        Map<String, Double> weights = inputs.weights;
        List<Map<String, Double>> ranking = new ArrayList<>();

        Map<String, Double> singleFlow = new HashMap<>();
        Map<String, Double> weightedFlows = new HashMap<>();
        Set<String> realAlternatives = flows.get(0).keySet();
        for (String alternative : realAlternatives) {
            double value = 0;
            for(int i = 1; i < flows.size() + 1; i++) {
                singleFlow = flows.get(i-1);
                String dm = String.valueOf(i);
                value += singleFlow.get(alternative) * weights.get(dm);
            }
            weightedFlows.put(alternative, Format(value));
        }

        return weightedFlows;
    }

    private static double Format(double number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
