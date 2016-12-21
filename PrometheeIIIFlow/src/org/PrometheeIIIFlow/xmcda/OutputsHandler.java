package org.PrometheeIIIFlow.xmcda;

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
            case "intervals":
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
            case "intervals":
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
    public static Map<String, XMCDA> convert(List<String> alternatives, Object[] table_results, ProgramExecutionResult executionResult)
    {
        Map<Pair<String, String>, String> alternativesComparison = (Map<Pair<String, String>, String>) table_results[0];
        Map<String, Pair<Double, Double>> intervals = (Map<String, Pair<Double, Double>>) table_results[1];

        final HashMap<String, XMCDA> x_results = new HashMap<>();
        XMCDA xmcda = new XMCDA();
        AlternativesMatrix<String> result = new AlternativesMatrix<String>();

        for(String alternative1: alternatives) {
            for (String alternative2 : alternatives) {
                if (alternativesComparison.keySet().contains(new Pair<String, String>(alternative1, alternative2))) {
                    String value = alternativesComparison.get(new Pair<String, String>(alternative1, alternative2));
                    Alternative alt1 = new Alternative(alternative1);
                    Alternative alt2 = new Alternative(alternative2);
                    Coord<Alternative, Alternative> coord = new Coord<Alternative, Alternative>(alt1, alt2);
                    QualifiedValues<String> values = new QualifiedValues<String>(new QualifiedValue<String>(value));
                    result.put(coord, values);
                }
            }
        }

        xmcda.alternativesMatricesList.add(result);
        x_results.put("ranking", xmcda);

        final HashMap<String, XMCDA> x_intervals = new HashMap<>();
        XMCDA xmcda2 = new XMCDA();
        AlternativesValues<LabelledQValues<Double>> intervals_xmcda = new AlternativesValues<>();

        for (String alternative_id : intervals.keySet()) {
            LabelledQValues<Double> intervals_val = new LabelledQValues<>();
            QualifiedValue<Double> x_int = new QualifiedValue<Double>(intervals.get(alternative_id).getKey());
            x_int.setId("LowerFlow");
            QualifiedValue<Double> y_int = new QualifiedValue<Double>(intervals.get(alternative_id).getValue());
            y_int.setId("UpperFlow");
            intervals_val.add(x_int);
            intervals_val.add(y_int);
            intervals_xmcda.put(new Alternative(alternative_id), intervals_val);
        }

        xmcda2.alternativesValuesList.add(intervals_xmcda);

        x_results.put("intervals", xmcda2);

        return x_results;
    }
}
