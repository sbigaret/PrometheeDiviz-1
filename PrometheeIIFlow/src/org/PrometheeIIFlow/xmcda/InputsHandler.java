package org.PrometheeIIFlow.xmcda;

import org.PrometheeIIFlow.structures.Triple;
import org.xmcda.*;
import org.xmcda.utils.ValueConverters;

import java.util.*;


public class InputsHandler {

    public enum ComparisonWithParam {
        ALTERNATIVES("alternatives"), BOUNDARY_PROFILES("boundary_profiles"), CENTRAL_PROFILES("central_profiles");

        private String label;

        private ComparisonWithParam(String paramLabel) {
            label = paramLabel;
        }

        public final String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static ComparisonWithParam fromString(String parameterLabel) {
            if (parameterLabel == null)
                throw new NullPointerException("parameterLabel is null");
            for (ComparisonWithParam op : ComparisonWithParam.values()) {
                if (op.toString().equals(parameterLabel))
                    return op;
            }
            throw new IllegalArgumentException("No enum ComparisonWithParam with label " + parameterLabel);
        }
    }


    public static class Inputs
    {
        public List<String> alternatives_ids;

        public Map<String, Double> positiveFlow;

        public Map<String, Double> negativeFlow;

        public ComparisonWithParam comparisonWith;

        public ArrayList<String> profiles_ids;

        private boolean exist_profiles = false;
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
        checkAndExtractParameters(inputs, xmcda, errors);
        checkCategoriesProfiles(inputs, xmcda, errors);

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


    private static void checkAndExtractParameters(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        ComparisonWithParam comparisonWith = null;

        if (xmcda.programParametersList.size() > 1) {
            errors.addError("Only one programParameters is expected");
            return;
        }
        if (xmcda.programParametersList.size() == 0) {
            errors.addError("No programParameter found");
            return;
        }
        if (xmcda.programParametersList.get(0).size() != 1) {
            errors.addError("Exactly one programParameters are expected");
            return;
        }

        final ProgramParameter<?> prgParam = xmcda.programParametersList.get(0).get(0);

        if (!"comparison_with".equals(prgParam.name())) {
            errors.addError(String.format("Invalid parameter w/ id '%s'", prgParam.id()));
            return;
        }

        if (prgParam.getValues() == null || (prgParam.getValues() != null && prgParam.getValues().size() != 1)) {
            errors.addError("Parameter operator must have a single (label) value only");
            return;
        }

        try {
            final String parameterValue = (String) prgParam.getValues().get(0).getValue();
            comparisonWith = ComparisonWithParam.fromString((String) parameterValue);
        } catch (Throwable throwable) {
            StringBuffer valid_values = new StringBuffer();
            for (ComparisonWithParam op : ComparisonWithParam.values()) {
                valid_values.append(op.getLabel()).append(", ");
            }
            String err = "Invalid value for parameter operator, it must be a label, ";
            err += "possible values are: " + valid_values.substring(0, valid_values.length() - 2);
            errors.addError(err);
            comparisonWith = null;
        }
        inputs.comparisonWith = comparisonWith;
    }

    private static void checkCategoriesProfiles(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        if (inputs.comparisonWith != ComparisonWithParam.ALTERNATIVES) {
            if (xmcda.categoriesProfilesList.size() == 0) {
                errors.addError("Categories profiles have not been supplied");
                return;
            }
            if (xmcda.categoriesProfilesList.size() != 1) {
                errors.addError("Exactly one performance table is expected");
                return;
            }
            if (xmcda.categoriesProfilesList.get(0).isEmpty()) {
                errors.addError("Categories Profiles list is empty");
                return;
            }
            inputs.exist_profiles = true;
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
        extractProfiles(inputs, xmcda, xmcda_execution_results);
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

        if (inputs.profiles_ids != null) {
            for (String profile : inputs.profiles_ids) {
                alternatives_ids.remove(profile);
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

                if(!inputs.exist_profiles) {
                    if (iterator < inputs.alternatives_ids.size()) {
                        if (inputs.alternatives_ids.contains(id)) {
                            positiveFlow.put(id, z);
                            iterator++;
                        }
                        else {
                            errors.addError("You have alternative/profile in your flows that is not in your input file with alternatives/profiles. Check this out.");
                        }
                    } else if (inputs.alternatives_ids.contains(id)) {
                        negativeFlow.put(id, z);
                        iterator++;
                    }
                    else {
                        errors.addError("You have alternative/profile in your flows that is not in your input file with alternatives/profiles. Check this out.");
                    }
                }
                else {
                    if (iterator < inputs.alternatives_ids.size() + inputs.profiles_ids.size()) {
                        if (inputs.alternatives_ids.contains(id) || inputs.profiles_ids.contains(id)) {
                            positiveFlow.put(id, z);
                            iterator++;
                        }
                        else {
                            errors.addError("You have alternative/profile in your flows that is not in your input file with alternatives/profiles. Check this out.");
                        }
                    } else if (inputs.alternatives_ids.contains(id) || inputs.profiles_ids.contains(id)) {
                        negativeFlow.put(id, z);
                        iterator++;
                    }
                    else {
                        errors.addError("You have alternative/profile in your flows that is not in your input file with alternatives/profiles. Check this out.");
                    }
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

    @SuppressWarnings("rawtypes")
    private static void extractProfiles(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        if (inputs.comparisonWith != ComparisonWithParam.ALTERNATIVES) {
            inputs.profiles_ids = new ArrayList<String>();
            for (CategoryProfile catProf : xmcda.categoriesProfilesList.get(0)) {
                if (inputs.comparisonWith == ComparisonWithParam.BOUNDARY_PROFILES) {
                    if ((catProf.getLowerBound() == null)&&(catProf.getUpperBound() == null)){
                        errors.addError("Upper Bound or Lower Bound Profile in categories profiles must be specified");
                        return;
                    }
                    if (catProf.getLowerBound() != null) {
                        if (catProf.getLowerBound().getAlternative() != null) {
                            inputs.profiles_ids.add(catProf.getLowerBound().getAlternative().id());
                        } else {
                            errors.addError("Alternative in Category Profile must be specified");
                            return;
                        }
                    }
                    if (catProf.getUpperBound() != null) {
                        if (catProf.getUpperBound().getAlternative() != null) {
                            inputs.profiles_ids.add(catProf.getUpperBound().getAlternative().id());
                        } else {
                            errors.addError("Alternative in Category Profile must be specified");
                            return;
                        }
                    }
                }
                if (inputs.comparisonWith == ComparisonWithParam.CENTRAL_PROFILES) {
                    if (catProf.getCentralProfile() != null) {
                        if (catProf.getCentralProfile().getAlternative() != null) {
                            inputs.profiles_ids.add(catProf.getCentralProfile().getAlternative().id());
                        } else {
                            errors.addError("Alternative in Category Profile must be specified");
                            return;
                        }
                    } else {
                        errors.addError("Central Profile in categories profiles must be specified");
                        return;
                    }
                }
            }
            if (inputs.profiles_ids.isEmpty()) {
                errors.addError("Profiles IDs is empty");
            }
        }
    }

}
