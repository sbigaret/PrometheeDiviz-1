package org.PrometheeGroupRanking.xmcda;

import org.xmcda.*;
import org.xmcda.utils.ValueConverters;

import java.util.*;

public class InputsHandler {

    public static class Inputs
    {
        public List<String> alternatives_ids;

        public List<Map<String, Double>> flows;

        public ArrayList<Double> weights;
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
        checkAlternatives(inputs, xmcda, errors);
        checkAlternativesValues(inputs, xmcda, errors);
        checkParameters(xmcda, errors);

        return inputs;
    }

    private static void checkParameters(XMCDA xmcda, ProgramExecutionResult errors) {

        if (xmcda.programParametersList.size() == 0) {
            errors.addError("No parameters found");
            return;
        }
    }

    private static void checkAlternatives(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {

        if (xmcda.alternatives.size() == 0) {
            errors.addError("No alternatives found");
            return;
        }
        if (xmcda.alternatives.getActiveAlternatives().size() == 0) {
            errors.addError("No active alternatives found");
            return;
        }
    }

    private static void checkAlternativesValues(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {

        if(xmcda.alternativesValuesList.size() == 0){
            errors.addError("No flows found");
            return;
        }
    }



    protected static Inputs extractInputs(Inputs inputs, XMCDA xmcda, ProgramExecutionResult xmcda_execution_results) throws ValueConverters.ConversionException {

        extractAlternatives(inputs, xmcda, xmcda_execution_results);
        extractParameters(inputs, xmcda, xmcda_execution_results);
        extractAlternativesValues(inputs, xmcda, xmcda_execution_results);

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

    private static void extractParameters(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException {

        ArrayList<Double> weights = new ArrayList<>();
        ProgramParameters<?> params  = xmcda.programParametersList.get(0);
        for(int i = 1; i <= params.size(); i++) {
            String id = params.get(i - 1).id();
            if (!("decisionMaker" + i).equalsIgnoreCase(id)) {
                errors.addError(String.format("Invalid parameter w/ id '%s'", id));
                return;
            }
            if (params.get(i - 1).getValues() == null || (params.get(i - 1).getValues() != null && params.get(i - 1).getValues().size() != 1)) {
                errors.addError("Parameter decisionMaker" + i + " must have a single (real) value only");
                return;
            }
            QualifiedValues<Double> weight = params.get(i - 1).getValues().convertToDouble();
            double w = weight.get(0).getValue();

            if (w < 0 || w > 1) {
                errors.addError("Improper value of weight for one of decision makers");
            }

            weights.add(w);
        }

        inputs.weights = weights;
    }

    private static void extractAlternativesValues(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException {

        Map<String, Double> singleFlow = new HashMap<>();
        List<Map<String, Double>> flows = new ArrayList<>();

        int iterator = 0;
        int decidentsIterator = 0;
        Set<Alternative> realAlternatives = xmcda.alternativesValuesList.get(0).getAlternatives();
        for(AlternativesValues flow : xmcda.alternativesValuesList) {
            Set<Alternative> alt = flow.getAlternatives();
            for(Alternative al : alt) {
                LabelledQValues x = (LabelledQValues) flow.get(al);
                QualifiedValue y = (QualifiedValue) x.get(0);
                double z = (double) y.getValue();

                String id = al.id();

                if (inputs.alternatives_ids.contains(id)) {
                    singleFlow.put(id, z);
                    if(iterator % inputs.alternatives_ids.size() == (realAlternatives.size()-1)) {
                        if(singleFlow.size() < realAlternatives.size()) {
                            errors.addError("There are too few flows values");
                        }
                        else {
                            flows.add(singleFlow);
                            singleFlow = new HashMap<>();
                            decidentsIterator++;
                        }
                    }
                    iterator++;
                }
                else {
                    errors.addError("You have alternative in your flows that is not in your input file with alternatives. Check this out.");
                }

            }
        }

        if(flows.size() < 2) {
            errors.addError("There should be at least two DMs and flows for them.");
        }

        if(flows.size() > inputs.weights.size()) {
            errors.addError("There are too few weights comparing to flows");
        }

        inputs.flows = flows;

    }
}

