package org.PrometheeIIFlow;

import org.PrometheeIIFlow.xmcda.InputsHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrometheeIIFlow {
    public static Map<String, Double> calculatePrometheeIIRanking(InputsHandler.Inputs inputs) {

        Map<String, Double> positiveFlow = inputs.positiveFlow;
        Map<String, Double> negativeFlow = inputs.negativeFlow;

        ArrayList<String> all_ids = new ArrayList<>();
        all_ids.addAll(inputs.alternatives_ids);
        if(inputs.comparisonWith != InputsHandler.ComparisonWithParam.ALTERNATIVES)
            all_ids.addAll(inputs.profiles_ids);

        Map<String, Double> flow = countFlow(inputs, all_ids);

        Map<String, Double> result = new LinkedHashMap<>();

        for (String alternativeA : all_ids) {
            for (String alternativeB : all_ids) {
                if(!alternativeA.equalsIgnoreCase(alternativeB)) {
                    /*if((positiveFlow.get(alternativeA) > positiveFlow.get(alternativeB) &&
                            negativeFlow.get(alternativeA) < negativeFlow.get(alternativeB)) ||
                            (positiveFlow.get(alternativeA) > positiveFlow.get(alternativeB) &&
                                    negativeFlow.get(alternativeA) == negativeFlow.get(alternativeB)) ||
                            (positiveFlow.get(alternativeA) == positiveFlow.get(alternativeB) &&
                                    negativeFlow.get(alternativeA) < negativeFlow.get(alternativeB)) ||
                            (positiveFlow.get(alternativeA) == positiveFlow.get(alternativeB) &&
                                    negativeFlow.get(alternativeA) == negativeFlow.get(alternativeB))) {*/

                        result.put(alternativeA, flow.get(alternativeA));

                   // }
                }
            }
        }
        return result;
    }

    private static Map<String, Double> countFlow(InputsHandler.Inputs inputs, ArrayList<String> all_ids) {

        Map<String, Double> positiveFlow = inputs.positiveFlow;
        Map<String, Double> negativeFlow = inputs.negativeFlow;

        Map<String, Double> flow = new LinkedHashMap<>();

        for (String alternative : all_ids) {
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
