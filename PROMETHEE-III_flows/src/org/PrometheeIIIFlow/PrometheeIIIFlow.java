package org.PrometheeIIIFlow;

import javafx.util.Pair;
import org.PrometheeIIIFlow.structures.Triple;
import org.PrometheeIIIFlow.xmcda.InputsHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PrometheeIIIFlow {
    public static Object[] calculatePrometheeIIIRanking(InputsHandler.Inputs inputs) {

        Map<String, Double> relativeFlow = new LinkedHashMap<>();
        Map<String, Double> sigma = new LinkedHashMap<>();
        Map<String, Double> x_table = new LinkedHashMap<>();
        Map<String, Double> y_table = new LinkedHashMap<>();
        Map<Pair<String, String>, Integer> result = new HashMap<>();
        Map<String, Pair<Double, Double>> intervals = new HashMap<>();
        Object[] table_results = new Object[2];
        table_results[0] = result;
        table_results[1] = intervals;

        Map<String, Double> flow = countFlow(inputs);

        int alternativesSize = inputs.alternatives_ids.size();

        for(Map.Entry<String, Double> tuple : flow.entrySet()) {
            double relative_flow = tuple.getValue()/alternativesSize;
            relativeFlow.put(tuple.getKey(), relative_flow);
        }

        for (String alternativeA : inputs.alternatives_ids) {
            for (String alternativeB : inputs.alternatives_ids) {
                if (!alternativeA.equalsIgnoreCase(alternativeB)) {
                    double current_sigma_value = sigma.getOrDefault(alternativeA, 0.0);
                    double temp = getValueFromAlternatives(alternativeA, alternativeB, inputs) -
                            getValueFromAlternatives(alternativeB, alternativeA, inputs) - flow.get(alternativeA);
                    double sigma_value = current_sigma_value + Math.pow(temp,2);
                    if(sigma.containsKey(alternativeA)) {
                        sigma.replace(alternativeA, current_sigma_value, sigma_value);
                    }
                    else {
                        sigma.put(alternativeA, sigma_value);
                    }
                }
            }
            sigma.replace(alternativeA, sigma.get(alternativeA), sigma.get(alternativeA)/alternativesSize);
            sigma.replace(alternativeA, sigma.get(alternativeA), Math.sqrt(sigma.get(alternativeA)));

            double temp_x = Format(flow.get(alternativeA) - inputs.alpha * sigma.get(alternativeA));
            double temp_y = Format(flow.get(alternativeA) + inputs.alpha * sigma.get(alternativeA));
            x_table.put(alternativeA, temp_x);
            y_table.put(alternativeA, temp_y);
            intervals.put(alternativeA, new Pair<>(temp_x, temp_y));
        }

        for (String alternativeA : inputs.alternatives_ids) {
            for (String alternativeB : inputs.alternatives_ids) {
                if (!alternativeA.equalsIgnoreCase(alternativeB)) {
                    if(x_table.get(alternativeA) > y_table.get(alternativeB) || (x_table.get(alternativeA) <= y_table.get(alternativeB) && x_table.get(alternativeB) <= y_table.get(alternativeA))) {
                        result.put(new Pair<>(alternativeA, alternativeB), 1);
                    }
                }
            }
        }

        return table_results;
    }

    public static double getValueFromAlternatives(String alternative1, String alternative2, InputsHandler.Inputs inputs) {
        for(Triple<String, String, Double> tuple : inputs.preferenceTable) {
            if(tuple.getFirst().equalsIgnoreCase(alternative1) && tuple.getSecond().equalsIgnoreCase(alternative2)) {
                return tuple.getThird();
            }
        }
        return -1;
    }

    private static Map<String, Double> countFlow(InputsHandler.Inputs inputs) {

        Map<String, Double> flow = new LinkedHashMap<>();

        for (String alternativeA : inputs.alternatives_ids) {
            double value = 0;
            for (String alternativeB : inputs.alternatives_ids) {
                if (!alternativeA.equalsIgnoreCase(alternativeB)) {
                    value += (getValueFromAlternatives(alternativeA, alternativeB, inputs) - getValueFromAlternatives(alternativeB, alternativeA, inputs));
                }
            }
            value = value/ inputs.alternatives_ids.size();
            flow.put(alternativeA, value);
        }

        return flow;
    }

    private static double Format(double number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

