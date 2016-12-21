package org.GroupClassAcceptabilities.xmcda;

import javafx.util.Pair;
import org.GroupClassAcceptabilities.structures.Triple;
import org.xmcda.*;
import org.xmcda.utils.Coord;
import org.xmcda.utils.Matrix;

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
            case "alternatives_support":
                return "alternativesValues";
            case "unimodal_alternatives_support":
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
            case "alternatives_support":
                return "alternativesValues";
            case "unimodal_alternatives_support":
                return "alternativesValues";
            case "messages":
                return "methodMessages";
            default:
                throw new IllegalArgumentException(String.format("Unknown output name '%s'",outputName));
        }
    }


    public static Map<String, XMCDA> convert(InputsHandler.Inputs inputs, Object[] results, ProgramExecutionResult executionResult)
    {
        List<Triple<String, String, String>> alternative_support_percentage = (List<Triple<String, String, String>>)results[0];
        List<Triple<String, String, String>> alternative_unimodal_support_percentage = (List<Triple<String, String, String>>)results[1];

        final HashMap<String, XMCDA> x_results1 = new HashMap<>();
        XMCDA xmcda1 = new XMCDA();
        AlternativesValues support = new AlternativesValues();

        Map<String, Map<String, String>> alt_support  = new HashMap<>();

        for(String alternative: inputs.alternatives_ids) {
            Map<String, String> pairs = new HashMap<>();
            for (Triple<String, String, String> triple : alternative_support_percentage) {
                if(alternative.equalsIgnoreCase(triple.getFirst())) {
                    pairs.put(triple.getSecond(), triple.getThird());
                }
            }
            alt_support.put(alternative, pairs);
            Alternative alt1 = new Alternative(alternative);
            LabelledQValues<String> values = new LabelledQValues<String>();
            for(String criteria : pairs.keySet()) {
                String value = pairs.get(criteria);
                QualifiedValue<String> qualifiedValue = new QualifiedValue<String>(value);
                qualifiedValue.setId(criteria);
                values.add(qualifiedValue);
            }

            support.put(alt1, values);
        }

        xmcda1.alternativesValuesList.add(support);
        x_results1.put("alternatives_support", xmcda1);

        XMCDA xmcda2 = new XMCDA();
        AlternativesValues unimodal_support = new AlternativesValues();

        Map<String, Map<String, String>> unimodal_alt_support  = new HashMap<>();

        for(String alternative: inputs.alternatives_ids) {
            Map<String, String> pairs = new HashMap<>();
            for (Triple<String, String, String> triple : alternative_unimodal_support_percentage) {
                if(alternative.equalsIgnoreCase(triple.getFirst())) {
                    pairs.put(triple.getSecond(), triple.getThird());
                }
            }
            unimodal_alt_support.put(alternative, pairs);
            Alternative alt1 = new Alternative(alternative);
            LabelledQValues<String> values = new LabelledQValues<String>();
            for(String criteria : pairs.keySet()) {
                String value = pairs.get(criteria);
                QualifiedValue<String> qualifiedValue = new QualifiedValue<String>(value);
                qualifiedValue.setId(criteria);
                values.add(qualifiedValue);
            }

            unimodal_support.put(alt1, values);
        }
        xmcda2.alternativesValuesList.add(unimodal_support);
        x_results1.put("unimodal_alternatives_support", xmcda2);

        return x_results1;

    }
}


