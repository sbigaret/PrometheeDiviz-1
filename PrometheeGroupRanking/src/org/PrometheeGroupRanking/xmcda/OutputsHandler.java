package org.PrometheeGroupRanking.xmcda;

import javafx.util.Pair;
import org.xmcda.*;
import org.xmcda.utils.Coord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputsHandler {
    /**
     * Returns the xmcda v3 tag for a given output
     * @param outputName the output's name
     * @return the associated XMCDA v2 tag
     * @throws NullPointerException if outputName is null
     * @throws IllegalArgumentException if outputName is not known
     */
    public static final String xmcdaV3Tag(String outputName)
    {
        switch(outputName)
        {
            case "ranking":
                return "alternativesMatrix";
            case "aggregatedFlows":
                return "alternativesMatrix";
            case "flows_2":
                return "alternativesValues";
            case "messages":
                return "programExecutionResult";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'",outputName));
        }
    }

    /**
     * Returns the xmcda v2 tag for a given output
     * @param outputName the output's name
     * @return the associated XMCDA v2 tag
     * @throws NullPointerException if outputName is null
     * @throws IllegalArgumentException if outputName is not known
     */
    public static final String xmcdaV2Tag(String outputName)
    {
        switch(outputName)
        {
            case "ranking":
                return "alternativesComparisons";
            case "aggregatedFlows":
                return "alternativesComparisons";
            case "flows_2":
                return "alternativesValues";
            case "messages":
                return "methodMessages";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'",outputName));
        }
    }


    /**
     * Converts the results of the computation step into XMCDA objects.
     * @param
     * @param executionResult
     * @return a map with keys being xmcda objects' names and values their corresponding XMCDA object
     */
    public static Map<String, XMCDA> convert(InputsHandler.Inputs inputs, List<Map<String, Double>> table_results, ProgramExecutionResult executionResult)
    {
        /*final HashMap<String, XMCDA> x_results = new HashMap<>();

        XMCDA xmcda = new XMCDA();
        AlternativesValues<Double> x_alternatives_values = new AlternativesValues<Double>();

        for(int i = 0; i < table_results.size(); i++) {
            x_alternatives_values =  new AlternativesValues<Double>();
            for (String alternative_id : table_results.get(i).keySet())
                x_alternatives_values.put(new Alternative(alternative_id), table_results.get(i).get(alternative_id));

            xmcda.alternativesValuesList.add(x_alternatives_values);

            int iterator = i + 1;
            String file = "flows_"+iterator;
            x_results.put(file, xmcda);
        }



        return x_results; */


        //Map<Pair<String, String>, String> alternativesComparison = (Map<Pair<String, String>, String>) table_results[0];
        //Map<String, Pair<Double, Double>> intervals = (Map<String, Pair<Double, Double>>) table_results[1];

        final HashMap<String, XMCDA> x_results = new HashMap<>();
        XMCDA xmcda = new XMCDA();
        AlternativesMatrix<Double> result = new AlternativesMatrix<Double>();

        for (int i = 0; i < table_results.size(); i++) {
            for(String alternative1: inputs.alternatives_ids) {
                if (table_results.get(i).keySet().contains(alternative1)) {
                    int noDm = i + 1;
                    Double value = table_results.get(i).get(alternative1);
                    Alternative alt1 = new Alternative(alternative1);
                    Alternative alt2 = new Alternative("DM"+noDm);
                    Coord<Alternative, Alternative> coord = new Coord<Alternative, Alternative>(alt1, alt2);
                    QualifiedValues<Double> values = new QualifiedValues<Double>(new QualifiedValue<Double>(value));
                    result.put(coord, values);
                }
            }
        }

        xmcda.alternativesMatricesList.add(result);
        x_results.put("ranking", xmcda);

        AlternativesMatrix<Double> aggregation = new AlternativesMatrix<Double>();
        xmcda = new XMCDA();
        for (int i = 0; i < inputs.flows.size(); i++) {
            for(String alternative1: inputs.alternatives_ids) {
                if (inputs.flows.get(i).keySet().contains(alternative1)) {
                    int noDm = i + 1;
                    Double value = inputs.flows.get(i).get(alternative1);
                    Alternative alt1 = new Alternative(alternative1);
                    Alternative alt2 = new Alternative("DM"+noDm);
                    Coord<Alternative, Alternative> coord = new Coord<Alternative, Alternative>(alt1, alt2);
                    QualifiedValues<Double> values = new QualifiedValues<Double>(new QualifiedValue<Double>(value));
                    aggregation.put(coord, values);
                }
            }
        }

        xmcda.alternativesMatricesList.add(aggregation);
        x_results.put("aggregatedFlows", xmcda);

        return x_results;

        /*final HashMap<String, XMCDA> x_intervals = new HashMap<>();
        XMCDA xmcda2 = new XMCDA();
        AlternativesValues<LabelledQValues<Double>> intervals_xmcda = new AlternativesValues<>();

        for (String alternative_id : intervals.keySet()) {
            LabelledQValues<Double> intervals_val = new LabelledQValues<>();
            QualifiedValue<Double> x_int = new QualifiedValue<Double>(intervals.get(alternative_id).getKey());
            x_int.setId("x_");
            QualifiedValue<Double> y_int = new QualifiedValue<Double>(intervals.get(alternative_id).getValue());
            y_int.setId("y_");
            intervals_val.add(x_int);
            intervals_val.add(y_int);
            intervals_xmcda.put(new Alternative(alternative_id), intervals_val);
        }

        xmcda2.alternativesValuesList.add(intervals_xmcda);

        x_results.put("intervals", xmcda2);

        return x_results;
        */
    }
}
