package org.NetFlowScore.xmcda;

import org.NetFlowScore.structures.Triple;
import org.xmcda.*;
import org.xmcda.utils.ValueConverters;

import java.util.*;

public class InputsHandler {

    /**
     * This class contains every element which are needed to compute the weighted sum.
     * It is populated by {@link InputsHandler#checkAndExtractInputs(XMCDA, ProgramExecutionResult)}.
     */
    public static class Inputs
    {
        public List<String> alternatives_ids;

        public Map<String, Double> positiveFlow;

        public Map<String, Double> negativeFlow;

        public ArrayList<Triple<String, String, Double>> preferenceTable;

        public String function;

        public String direction;
    }


    /**
     *
     * @param xmcda
     * @param xmcda_exec_results
     * @return
     */
    static public Inputs checkAndExtractInputs(XMCDA xmcda, ProgramExecutionResult xmcda_exec_results) throws ValueConverters.ConversionException, ClassNotFoundException {
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
     * @return Inputs
     */
    protected static Inputs checkInputs(XMCDA xmcda, ProgramExecutionResult errors)
    {
        Inputs inputs = new Inputs();
        checkAlternatives(inputs, xmcda, errors);
        checkParameters(inputs, xmcda, errors);
        checkPreferences(inputs, xmcda, errors);

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
        inputs.alternatives_ids = alternatives_ids;
        if (alternatives_ids.isEmpty()) {
            errors.addError("IDs are empty");
        }
    }

    private static void extractParameters(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException, ClassNotFoundException {
        String string = "";
        Class c = Class.forName("java.lang.String");

        ProgramParameters<?> params  = xmcda.programParametersList.get(0);
        QualifiedValues<?> alpha = params.get(0).getValues().convertTo(c);
        QualifiedValues<?> beta = params.get(1).getValues().convertTo(c);

        String al = (String) alpha.get(0).getValue();
        String b = (String) beta.get(0).getValue();

        inputs.function = al;
        inputs.direction = b;

        if (al.isEmpty() || b.isEmpty()) {
            errors.addError("params are empty");
        }
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

        inputs.preferenceTable = preferenceTable;

        if (preferenceTable.size() == 0) {
            errors.addError("preference table is empty");
        }
    }
}