package org.PrometheeIIIFlow.xmcda;

import org.PrometheeIIIFlow.structures.Triple;
import org.xmcda.*;
import org.xmcda.utils.Coord;
import org.xmcda.utils.ValueConverters;

import java.util.*;

public class InputsHandler {

    public static class Inputs
    {
        public List<String> alternatives_ids;

        public Map<String, Double> positiveFlow;

        public Map<String, Double> negativeFlow;

        public ArrayList<Triple<String, String, Double>> preferenceTable;

        public double alpha;
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
        checkParameters(xmcda, errors);
        checkPreferences(xmcda, errors);

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

    private static void checkParameters(XMCDA xmcda, ProgramExecutionResult errors) {

        if (xmcda.programParametersList.size() == 0) {
            errors.addError("No parameters found");
            return;
        }
    }

    private static void checkPreferences(XMCDA xmcda, ProgramExecutionResult errors) {

        if (xmcda.alternativesMatricesList.size() == 0 || xmcda.alternativesMatricesList.get(0).size() == 0) {
            errors.addError("No preferences found");
            return;
        }
    }


    protected static Inputs extractInputs(Inputs inputs, XMCDA xmcda, ProgramExecutionResult xmcda_execution_results) throws ValueConverters.ConversionException {

        extractAlternatives(inputs, xmcda, xmcda_execution_results);
        extractParameters(inputs, xmcda, xmcda_execution_results);
        extractPreferences(inputs, xmcda, xmcda_execution_results);

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

        ProgramParameters<?> params  = xmcda.programParametersList.get(0);
        QualifiedValues<Double> alpha = params.get(0).getValues().convertToDouble();
        double al = alpha.get(0).getValue();

        if (al == 0) {
            errors.addError("params are empty");
        }

        inputs.alpha = al;
    }

    private static void extractPreferences(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException {

        ArrayList<Triple<String, String, Double>> preferenceTable = new ArrayList<>();

        for(Alternative a : xmcda.alternatives) {
            for(Alternative b: xmcda.alternatives) {
                if(xmcda.alternativesMatricesList.get(0).containsKey(new Coord<>(a,b))) {
                    QualifiedValues<?> v = xmcda.alternativesMatricesList.get(0).get(a, b).convertToDouble();
                    double w = v.get(0).convertToDouble().getValue();

                    Triple<String, String, Double> tuple = new Triple<>(a.id(), b.id(), w);
                    preferenceTable.add(tuple);
                }
                else {
                    errors.addError("You do not have enough values in Your preferences");
                }
            }
        }


        if (preferenceTable.size() == 0) {
            errors.addError("preference table is empty");
        } else if(preferenceTable.size() != xmcda.alternatives.size() * xmcda.alternatives.size()) {
            errors.addError("There are too few values in your preference table");
        }

        inputs.preferenceTable = preferenceTable;
    }
}
