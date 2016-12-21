package org.PrometheeIRanking;

import javafx.util.Pair;
import org.PrometheeIRanking.xmcda.InputsHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class PrometheeIRanking {
    public static Map<Pair<String, String>, String> calculatePrometheeIRanking(InputsHandler.Inputs inputs) {

        Map<String, Double> positiveFlow = inputs.positiveFlow;
        Map<String, Double> negativeFlow = inputs.negativeFlow;

        Map<String, Double> flow = countFlow(inputs);

        Map<Pair<String, String>,String> result = new HashMap<>();

        for (String alternativeA : inputs.alternatives_ids) {
            for (String alternativeB : inputs.alternatives_ids) {
                if(!alternativeA.equalsIgnoreCase(alternativeB)) {
                    if((positiveFlow.get(alternativeA) > positiveFlow.get(alternativeB) &&
                        negativeFlow.get(alternativeA) < negativeFlow.get(alternativeB)) ||
                        (positiveFlow.get(alternativeA) > positiveFlow.get(alternativeB) &&
                        negativeFlow.get(alternativeA).equals(negativeFlow.get(alternativeB))) ||
                        (positiveFlow.get(alternativeA).equals(positiveFlow.get(alternativeB)) &&
                        negativeFlow.get(alternativeA) < negativeFlow.get(alternativeB)) ||
                        (positiveFlow.get(alternativeA).equals(positiveFlow.get(alternativeB))) &&
                        (negativeFlow.get(alternativeA).equals(negativeFlow.get(alternativeB)))) {
                            //result.put(alternativeA, flow.get(alternativeA));
                            result.put(new Pair<>(alternativeA, alternativeB), "S");
                    }
                }
            }
        }
        return result;
    }

    private static Map<String, Double> countFlow(InputsHandler.Inputs inputs) {

        Map<String, Double> positiveFlow = inputs.positiveFlow;
        Map<String, Double> negativeFlow = inputs.negativeFlow;

        Map<String, Double> flow = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            double currentFlow = Format(positiveFlow.get(alternative) - negativeFlow.get(alternative));
            flow.put(alternative, currentFlow);
        }

        return flow;
    }

    private static double Format(double number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
