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
                return "alternativesValues";
            case "aggregated_flows":
                return "alternativesMatrix";
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
                return "alternativesValues";
            case "aggregated_flows":
                return "alternativesComparisons";
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
    public static Map<String, XMCDA> convert(InputsHandler.Inputs inputs, Map<String, Double> table_results, ProgramExecutionResult executionResult)
    {

        final HashMap<String, XMCDA> x_results = new HashMap<>();
        XMCDA xmcda = new XMCDA();
        AlternativesValues<Double> result = new AlternativesValues<Double>();

        for (int i = 0; i < table_results.size(); i++) {
            for(String alternative1: inputs.alternatives_ids) {
                if (table_results.keySet().contains(alternative1)) {
                    int noDm = i + 1;
                    Alternative alt1 = new Alternative(alternative1);
                    Double value = table_results.get(alternative1);
                    LabelledQValues<Double> values = new LabelledQValues<>(new QualifiedValue<Double>(value));
                    result.put(alt1, values);
                }
            }
        }

        xmcda.alternativesValuesList.add(result);
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
        x_results.put("aggregated_flows", xmcda);

        return x_results;

    }
}
