package org.GroupClassAcceptabilities.xmcda;

import org.GroupClassAcceptabilities.structures.Triple;
import org.xmcda.*;
import org.xmcda.utils.ValueConverters;
import org.xmcda.v2.CategoryValue;

import java.util.*;

public class InputsHandler {


    public static class Inputs
    {
        public List<String> alternatives_ids;

        public Map<Integer, List<Triple<String, Integer, Integer>>> dmsAssignments;

        public Map<String, Integer> categoriesValues;

        public Set<String> categories;
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
        checkCategories(xmcda, errors);
        checkAlternativesAssignments(xmcda, errors);

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

    private static void checkCategories(XMCDA xmcda, ProgramExecutionResult errors) {
        if(xmcda.categoriesValuesList.size() == 0) {
            errors.addError("No categories found");
            return;
        }
    }

    private static void checkAlternativesAssignments(XMCDA xmcda, ProgramExecutionResult errors) {
        if(xmcda.alternativesAssignmentsList.size() == 0) {
            errors.addError("No alternatives assignments found");
            return;
        }
    }


    protected static Inputs extractInputs(Inputs inputs, XMCDA xmcda, ProgramExecutionResult xmcda_execution_results) throws ValueConverters.ConversionException {
        extractAlternatives(inputs, xmcda, xmcda_execution_results);
        extractCategories(inputs, xmcda, xmcda_execution_results);
        extractAlternativesAssignments(inputs, xmcda, xmcda_execution_results);

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

    private static void extractCategories(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) throws ValueConverters.ConversionException {
        Map<String, Integer> categories_values = new HashMap<>();
        Set<String> categories = new HashSet<>();

        int min = Integer.MAX_VALUE;
        int max = -Integer.MAX_VALUE;

        CategoriesValues<Integer> categoriesValues = xmcda.categoriesValuesList.get(0).convertTo(Integer.class);

        for(Map.Entry<Category, LabelledQValues<Integer>> category: categoriesValues.entrySet()) {
            if (category.getValue().get(0).getValue() < min) {
                min = category.getValue().get(0).getValue();
            }
            if (category.getValue().get(0).getValue() > max) {
                max = category.getValue().get(0).getValue();
            }
            categories.add(category.getKey().id());
            categories_values.put(category.getKey().id(), category.getValue().get(0).getValue());
        }

        if (min != 1) {
            errors.addError("Minimal rank should be equal to 1.");
            return;
        }

        if (max != categories_values.size()) {
            errors.addError("Maximal rank should be equal to number of categories.");
            return;
        }

        for (Map.Entry categoryA : categoriesValues.entrySet()) {
            for (Map.Entry categoryB : categoriesValues.entrySet()) {
                if (categoryA.getValue() == categoryB.getValue() && categoryA.getKey() != categoryB.getKey()) {
                    errors.addError("There cannot be two categories with the same rank.");
                    return;
                }
            }
        }

        inputs.categories = categories;
        inputs.categoriesValues = categories_values;

    }

    private static void extractAlternativesAssignments(Inputs inputs, XMCDA xmcda, ProgramExecutionResult errors) {
        Map<Integer, List<Triple<String, Integer, Integer>>> alternatives_assignments = new HashMap<>();
        int dmsIterator = 1;
        for(AlternativesAssignments alt_assign: xmcda.alternativesAssignmentsList) {
            List<Triple<String, Integer, Integer>> assignments = new ArrayList<>();
            for(Object single_assign: alt_assign) {
                String alternative_id = ((AlternativeAssignment) single_assign).getAlternative().id();
                String lower_bound = ((AlternativeAssignment) single_assign).getCategoryInterval().getLowerBound().id();
                int lower_bound_value = inputs.categoriesValues.get(lower_bound).intValue();
                String upper_bound = ((AlternativeAssignment) single_assign).getCategoryInterval().getUpperBound().id();
                int upper_bound_value = inputs.categoriesValues.get(upper_bound).intValue();

                if(lower_bound_value > upper_bound_value) {
                    errors.addError("Lower bound cannot be greater than upper bound class assignment.");
                }

                assignments.add(new Triple<String, Integer, Integer>(alternative_id, lower_bound_value, upper_bound_value));
            }

            alternatives_assignments.put(dmsIterator, assignments);
            dmsIterator++;
        }

        if(alternatives_assignments.size() == 0) {
            errors.addError("There are no assignments");
        }

        inputs.dmsAssignments = alternatives_assignments;
    }

}

