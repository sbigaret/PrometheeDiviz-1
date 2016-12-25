package org.NetFlowScoreIterative.xmcda;

import org.NetFlowScoreIterative.structures.Triple;
import org.xmcda.*;
import org.xmcda.utils.ValueConverters;

import java.util.*;

public class InputsHandler {

    public static class Inputs
    {
        public List<String> alternatives_ids;

        public Map<String, Double> positiveFlow;

        public Map<String, Double> negativeFlow;

        public ArrayList<Triple<String, String, Double>> preferenceTable;

        public String function;

        public String direction;
    }


    static public Inputs checkAndExtractInputs(XMCDA xmcda, ProgramExecutionResult xmcda_exec_results) throws ValueConverters.ConversionException, ClassNotFoundException {
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

    protected static Inputs extractInputs(Inputs inputs, XMCDA xmcda, ProgramExecutionResult xmcda_execution_results) throws ValueConverters.ConversionException, ClassNotFoundException {
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

    private static void extractParameters(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException, ClassNotFoundException {
        String string = "";
        Class c = Class.forName("java.lang.String");

        ProgramParameters<?> params  = xmcda.programParametersList.get(0);
        QualifiedValues<?> alpha = params.get(0).getValues().convertTo(c);
        QualifiedValues<?> beta = params.get(1).getValues().convertTo(c);

        String al = (String) alpha.get(0).getValue();
        String b = (String) beta.get(0).getValue();


        if (al.isEmpty() || b.isEmpty()) {
            errors.addError("params are empty");
        }

        if(!(al.equalsIgnoreCase("max") || al.equalsIgnoreCase("min")||al.equalsIgnoreCase("sum"))) {
            errors.addError("There is wrong parameter for function.");
        }

        if(!(b.equalsIgnoreCase("in favor") || b.equalsIgnoreCase("against")||b.equalsIgnoreCase("difference"))) {
            errors.addError("There is wrong parameter for direction.");
        }

        inputs.function = al;
        inputs.direction = b;
    }

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

        if (preferenceTable.size() == 0) {
            errors.addError("preference table is empty");
        }

        if(preferenceTable.size() != xmcda.alternatives.size() * xmcda.alternatives.size()) {
            errors.addError("There is wrong number of values in preference table");
        }

        inputs.preferenceTable = preferenceTable;
    }

}
