package org.GroupClassAcceptabilities;

import org.GroupClassAcceptabilities.structures.Triple;
import org.GroupClassAcceptabilities.xmcda.InputsHandler;

import java.util.*;

public class GroupClassAcceptabilities {

    public static Object[] calculateGroupClassAcceptabilities(InputsHandler.Inputs inputs) {
        Object[] result = new Object[2];

        List<Triple<String, String, Integer>> dm_classes_membership = new ArrayList<>();
        Map<Integer, List<Triple<String, String, Integer>>> dms_memberships = new HashMap<>();

        int dms_iterator = 1;
        for(List<Triple<String, Integer, Integer>> list : inputs.dmsAssignments.values()) {
            for(Triple<String, Integer, Integer> triple : list) {
                for(String category : inputs.categoriesValues.keySet()) {
                    if(triple.getSecond() <= inputs.categoriesValues.get(category) && triple.getThird() >= inputs.categoriesValues.get(category)) {
                        dm_classes_membership.add(new Triple<>(triple.getFirst(), category, 1));
                    }
                    else {
                        dm_classes_membership.add(new Triple<>(triple.getFirst(), category, 0));
                    }
                }
            }
            dms_memberships.put(dms_iterator, dm_classes_membership);
            dm_classes_membership = new ArrayList<>();
            dms_iterator++;
        }

        // alternative | category | support as a double value
        List<Triple<String, String, Double>> alternative_support = new ArrayList<>();

        //alternative | category | percentage as a string -> output file
        List<Triple<String, String, String>> alternative_support_percentage = new ArrayList<>();

        for(String category: inputs.categories) {
            for(String alternative: inputs.alternatives_ids) {
                int sum_memberships = 0;
                for(List<Triple<String, String, Integer>> list : dms_memberships.values()) {
                    for(Triple<String, String, Integer> triple : list) {
                        if(triple.getFirst().equalsIgnoreCase(alternative) && triple.getSecond().equalsIgnoreCase(category)) {
                            sum_memberships += triple.getThird();
                        }
                    }
                }
                double value = (double)sum_memberships/(double)dms_memberships.size();
                int percentage_value = (int)(value * 100);
                String percentage = percentage_value + "%";

                alternative_support.add(new Triple<>(alternative, category, value));
                alternative_support_percentage.add(new Triple<>(alternative, category, percentage));
            }
        }

        //alternative | category | percentage as a string -> output file
        List<Triple<String, String, String>> alternative_unimodal_support_percentage = new ArrayList<>();

        for(Triple<String, String, Double> triple : alternative_support) {
            if( inputs.categoriesValues.get(triple.getSecond()) == 1 || inputs.categoriesValues.get(triple.getSecond()) == inputs.categoriesValues.size()) {
                double percentage_value = triple.getThird() * 100;
                String percentage = (int)percentage_value + "%";
                alternative_unimodal_support_percentage.add(new Triple<>(triple.getFirst(), triple.getSecond(), percentage));
            }
            else {
                double max_lower = getMaxFromLowerOrGreater(inputs.categoriesValues, alternative_support, triple.getSecond(), triple.getFirst(), true);
                double max_greater = getMaxFromLowerOrGreater(inputs.categoriesValues, alternative_support, triple.getSecond(), triple.getFirst(), false);

                double final_value = Math.max(triple.getThird(), Math.min(max_lower, max_greater));
                int final_percentage_value = (int) (final_value * 100);
                String final_percentage = final_percentage_value + "%";
                alternative_unimodal_support_percentage.add(new Triple<>(triple.getFirst(), triple.getSecond(), final_percentage));
            }
        }

        result[0] = alternative_support_percentage;
        result[1] = alternative_unimodal_support_percentage;

        return result;
    }

    private static double getMaxFromLowerOrGreater(Map<String, Integer> categories_values, List<Triple<String, String, Double>> dms_support, String category, String alternative, boolean getLower) {

        List<Double> lowerOrGreaterTable = new ArrayList<>();
        for(Triple<String, String, Double> triple : dms_support) {
            if(triple.getFirst().equalsIgnoreCase(alternative)) {
                if (getLower) {
                    if (categories_values.get(category) > categories_values.get(triple.getSecond())) {
                        lowerOrGreaterTable.add(triple.getThird());
                        }
                } else {
                    if (categories_values.get(category) < categories_values.get(triple.getSecond())) {
                        lowerOrGreaterTable.add(triple.getThird());
                    }
                }
            }
        }

        double max = Collections.max(lowerOrGreaterTable);

        return max;

    }

}
