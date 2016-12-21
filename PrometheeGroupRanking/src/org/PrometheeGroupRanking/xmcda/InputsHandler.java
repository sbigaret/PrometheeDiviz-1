package org.PrometheeGroupRanking.xmcda;

import org.xmcda.*;
import org.xmcda.utils.ValueConverters;

import java.util.*;

public class InputsHandler {

    public static class Inputs
    {
        public List<String> alternatives_ids;

        public List<Map<String, Double>> flows;

        //public Map<String, Double> negativeFlow;

        public List<Map<String, Double>> weights;

        //public ArrayList<Triple<String, String, Double>> preferenceTable;

        //public double alpha;
    }


    /**
     *
     * @param xmcda
     * @param xmcda_exec_results
     * @return
     */
    static public Inputs checkAndExtractInputs(XMCDA xmcda, ProgramExecutionResult xmcda_exec_results) throws ValueConverters.ConversionException {
        Inputs inputsDict = checkInputs(xmcda, xmcda_exec_results);

        if ( xmcda_exec_results.isError() )
            return null;

        return extractInputs(inputsDict, xmcda, xmcda_exec_results);
    }


    /**
     * Checks the inputs
     *
     * @param xmcda
     * @param errors
     * @return
     */
    protected static Inputs checkInputs(XMCDA xmcda, ProgramExecutionResult errors)
    {
        Inputs inputs = new Inputs();
        checkAlternatives(inputs, xmcda, errors);
        checkAlternativesValues(inputs, xmcda, errors);
        //checkParameters(inputs, xmcda, errors);
        //checkPreferences(inputs, xmcda, errors);

        return inputs;
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

    private static void checkParameters(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {

        if (xmcda.programParametersList.size() == 0) {
            errors.addError("No parameters found");
            return;
        }
    }

    private static void checkPreferences(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {

        if (xmcda.alternativesMatricesList.size() == 0 || xmcda.alternativesMatricesList.get(0).size() == 0) {
            errors.addError("No preferences found");
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
        extractAlternativesValues(inputs, xmcda, xmcda_execution_results);
        //extractParameters(inputs, xmcda, xmcda_execution_results);
        //extractPreferences(inputs, xmcda, xmcda_execution_results);

        return inputs;
    }

    private static void extractAlternatives(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {

        List<String> alternatives_ids = new ArrayList<>();
        for (Alternative alternative : xmcda.alternatives) {
            if (alternative.isActive()) {
                alternatives_ids.add(alternative.id());
            }
        }
        inputs.alternatives_ids = alternatives_ids;
        if (alternatives_ids.isEmpty()) {
            errors.addError("IDs are empty");
        }
    }

    private static void extractAlternativesValues(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException {

        Map<String, Double> singleFlow = new HashMap<>();
        Map<String, Double> singleWeights = new HashMap<>();
        List<Map<String, Double>> weights = new ArrayList<>();
        List<Map<String, Double>> flows = new ArrayList<>();

        int iterator = 0;
        int decidentsIterator = 0;
        for(AlternativesValues flow : xmcda.alternativesValuesList) {
            //Object id = flow.get(0);
            Set<Alternative> alt = flow.getAlternatives();
            for(Alternative al : alt) {
                LabelledQValues x = (LabelledQValues) flow.get(al);
                QualifiedValue y = (QualifiedValue) x.get(0);
                double z = (double) y.getValue();

                String id = al.id();

                if(iterator < inputs.alternatives_ids.size() * xmcda.alternativesValuesList.size()/2) {
                    if (inputs.alternatives_ids.contains(id)) {
                        singleFlow.put(id, z);
                        System.out.println("^^^^ " + singleFlow);
                        if(iterator % inputs.alternatives_ids.size() == (inputs.alternatives_ids.size()-1)) {
                            if(singleFlow.size() < inputs.alternatives_ids.size()) {
                                errors.addError("There are too few weights");
                            }
                            else {
                                flows.add(singleFlow);
                                singleFlow = new HashMap<>();
                                decidentsIterator++;
                            }
                        }
                        iterator++;
                    }
                }
                else if(inputs.alternatives_ids.contains(id)) {
                    singleWeights.put(id, z);
                    System.out.println(singleWeights);
                    if(iterator % inputs.alternatives_ids.size() == (inputs.alternatives_ids.size()-1) && iterator > inputs.alternatives_ids.size()) {
                        if(singleWeights.size() < inputs.alternatives_ids.size()) {
                            errors.addError("There are too few weights");
                        }
                        else {
                            weights.add(singleWeights);
                            singleWeights = new HashMap<>();
                            decidentsIterator--;
                        }
                    }
                    iterator++;
                }
            }
        }
        System.out.println("FLOWS " + flows);
        System.out.println("WEIGHTS " + weights);
        if (decidentsIterator != 0) {
            errors.addError("There are to few values in flows or weights");
        }
        else {
            //weights.add(singleWeights);
            //flows.add(singleFlow);

            inputs.flows = flows;
            inputs.weights = weights;
        }
    }
/*
    private static void extractParameters(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException {

        ProgramParameters<?> params  = xmcda.programParametersList.get(0);
        QualifiedValues<Double> alpha = params.get(0).getValues().convertToDouble();
        double al = alpha.get(0).getValue();

        inputs.alpha = al;

        if (al == 0) {
            errors.addError("params are empty");
        }
    }
/*
    private static void extractPreferences(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException {

        ArrayList<Triple<String, String, Double>> preferenceTable = new ArrayList<>();

        for(Alternative a : xmcda.alternatives) {
            for(Alternative b: xmcda.alternatives) {
                QualifiedValues<?> v = xmcda.alternativesMatricesList.get(0).get(a, b).convertToDouble();
                double w = v.get(0).convertToDouble().getValue();

                Triple<String, String, Double> tuple = new Triple<>(a.id(), b.id(), w);
                preferenceTable.add(tuple);
            }
        }

        inputs.preferenceTable = preferenceTable;

        if (preferenceTable.size() == 0) {
            errors.addError("preference table is empty");
        }
    } */
}

