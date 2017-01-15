package org.PrometheeIRanking.xmcda;

import javafx.util.Pair;
import org.xmcda.*;
import org.xmcda.utils.Coord;

import java.util.HashMap;
import java.util.HashSet;
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
            case "messages":
                return "methodMessages";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'",outputName));
        }
    }


    /**
     * Converts the results of the computation step into XMCDA objects.
     * @param alternativesComparison
     * @param executionResult
     * @return a map with keys being xmcda objects' names and values their corresponding XMCDA object
     */
    public static Map<String, XMCDA> convert(List<String> alternatives, Map<Pair<String, String>, Integer> alternativesComparison, ProgramExecutionResult executionResult)
    {
        final HashMap<String, XMCDA> x_results = new HashMap<>();
        XMCDA xmcda = new XMCDA();
        AlternativesMatrix<Double> result = new AlternativesMatrix<Double>();

        for(String alternative1: alternatives) {
            for (String alternative2 : alternatives) {
                if (alternativesComparison.keySet().contains(new Pair<String, String>(alternative1, alternative2))) {
                    Double value = alternativesComparison.get(new Pair<String, String>(alternative1, alternative2)).doubleValue();
                    Alternative alt1 = new Alternative(alternative1);
                    Alternative alt2 = new Alternative(alternative2);
                    Coord<Alternative, Alternative> coord = new Coord<Alternative, Alternative>(alt1, alt2);
                    QualifiedValues<Double> values = new QualifiedValues<Double>(new QualifiedValue<Double>(value));
                    result.put(coord, values);
                }
            }
        }

        result.setMcdaConcept("atLeastAsGoodAs");
        xmcda.alternativesMatricesList.add(result);
        x_results.put("ranking", xmcda);

        return x_results;
    }
}
