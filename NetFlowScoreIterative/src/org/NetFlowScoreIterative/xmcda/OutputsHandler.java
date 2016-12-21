package org.NetFlowScoreIterative.xmcda;

import org.xmcda.*;

import java.util.HashMap;
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
            case "messages":
                return "methodMessages";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'",outputName));
        }
    }


    /**
     * Converts the results of the computation step into XMCDA objects.
     * @param alternativesValues
     * @param executionResult
     * @return a map with keys being xmcda objects' names and values their corresponding XMCDA object
     */
    public static Map<String, XMCDA> convert(Map<String, Integer> alternativesValues, ProgramExecutionResult executionResult)
    {
        final HashMap<String, XMCDA> x_results = new HashMap<>();

        XMCDA xmcda = new XMCDA();
        AlternativesValues<Integer> x_alternatives_values = new AlternativesValues<Integer>();

        for (String alternative_id : alternativesValues.keySet())
            x_alternatives_values.put(new Alternative(alternative_id), alternativesValues.get(alternative_id));

        xmcda.alternativesValuesList.add(x_alternatives_values);

        x_results.put("ranking", xmcda);

        return x_results;
    }
}

