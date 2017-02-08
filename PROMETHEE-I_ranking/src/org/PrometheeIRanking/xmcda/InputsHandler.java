package org.PrometheeIRanking.xmcda;

import org.xmcda.*;
import org.xmcda.utils.ValueConverters;

import java.util.*;

public class InputsHandler {

    public static class Inputs
    {
        public List<String> alternatives_ids;

        public Map<String, Double> positiveFlow;

        public Map<String, Double> negativeFlow;
    }


    static public Inputs checkAndExtractInputs(XMCDA xmcda, ProgramExecutionResult xmcda_exec_results) throws ValueConverters.ConversionException {
        Inputs inputsDict = checkInputs(xmcda, xmcda_exec_results);

        if ( xmcda_exec_results.isError() )
            return null;

        return extractInputs(inputsDict, xmcda, xmcda_exec_results);
    }


    protected static Inputs checkInputs(XMCDA xmcda, ProgramExecutionResult errors)
    {
        Inputs inputs = new Inputs();
        checkAlternatives(xmcda, errors);
        checkAlternativesValues(xmcda, errors);

        return inputs;
    }

    private static void checkAlternatives(XMCDA xmcda, ProgramExecutionResult errors) {

        if (xmcda.alternatives.size() == 0) {
            errors.addError("No alternatives found");
            return;
        }
        if (xmcda.alternatives.getActiveAlternatives().size() == 0) {
            errors.addError("No active alternatives found");
            return;
        }
    }

    private static void checkAlternativesValues(XMCDA xmcda, ProgramExecutionResult errors) {

        if(xmcda.alternativesValuesList.size() == 0){
            errors.addError("No flows found");
            return;
        }
    }


    /**
     *
     * @param inputs
     * @param xmcda
     * @param xmcda_execution_results
     * @return
     */
    protected static Inputs extractInputs(Inputs inputs, XMCDA xmcda, ProgramExecutionResult xmcda_execution_results) throws ValueConverters.ConversionException {

        extractAlternatives(inputs, xmcda, xmcda_execution_results);
        extractFlows(inputs, xmcda, xmcda_execution_results);

        return inputs;
    }

    private static void extractAlternatives(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {

        List<String> alternatives_ids = new ArrayList<>();
        for (Alternative alternative : xmcda.alternatives) {
            if (alternative.isActive()) {
                alternatives_ids.add(alternative.id());
            }
        }

        if (alternatives_ids.isEmpty()) {
            errors.addError("IDs are empty");
        }

        inputs.alternatives_ids = alternatives_ids;
    }

    private static void extractFlows(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException {

        Map<String, Double> positiveFlow = new HashMap<>();
        Map<String, Double> negativeFlow = new HashMap<>();

        int iterator = 0;
        for(AlternativesValues flow : xmcda.alternativesValuesList) {
            Set<Alternative> alt = flow.getAlternatives();
            for(Alternative al : alt) {
                LabelledQValues x = (LabelledQValues) flow.get(al);
                QualifiedValue y = (QualifiedValue) x.get(0);
                double z = (double) y.getValue();

                String id = al.id();

                if(iterator < inputs.alternatives_ids.size()) {
                    if (inputs.alternatives_ids.contains(id)) {
                        positiveFlow.put(id, z);
                        iterator++;
                    }
                    else {
                        errors.addError("You have alternative in your flows that is not in your input file with alternatives. Check this out.");
                    }
                }
                else if(inputs.alternatives_ids.contains(id)) {
                    negativeFlow.put(id, z);
                    iterator++;
                }
                else {
                    errors.addError("You have alternative in your flows that is not in your input file with alternatives. Check this out.");
                }
            }
        }

        if (negativeFlow.size() < inputs.alternatives_ids.size() || positiveFlow.size() < inputs.alternatives_ids.size()) {
            errors.addError("There are to few values in flows");
        }
        else {
            inputs.positiveFlow = positiveFlow;
            inputs.negativeFlow = negativeFlow;
        }
    }
}
