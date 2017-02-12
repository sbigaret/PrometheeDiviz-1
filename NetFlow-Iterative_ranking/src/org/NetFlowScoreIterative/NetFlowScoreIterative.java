package org.NetFlowScoreIterative;

import org.NetFlowScoreIterative.structures.ListMapsComparer;
import org.NetFlowScoreIterative.structures.Triple;
import org.NetFlowScoreIterative.structures.TripleComparer;
import org.NetFlowScoreIterative.xmcda.InputsHandler;
import org.xmcda.ProgramExecutionResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class NetFlowScoreIterative {
    public static Map<String, Integer> calculateNetFlowScoreIterative(InputsHandler.Inputs inputs, ProgramExecutionResult executionResult) {
        Map<String, Double> result = new LinkedHashMap<>();

        result = countResult(inputs.function, inputs.direction, inputs.alternatives_ids, inputs.preferenceTable, executionResult);

        List<Map<String, Double>> sameValues = getSameValuesOnList(result);

        ListMapsComparer listMapsComparer = new ListMapsComparer();
        Collections.sort(sameValues, listMapsComparer);

        Map<String, Integer> secondIteration = new LinkedHashMap<>();

        int shift = 1;
        for(Map<String, Double> sameValuesTuples : sameValues) {

                Map<String, Integer> secondIterationPartial = countPartialResults(sameValuesTuples, inputs, executionResult, shift);

                shift += sameValuesTuples.size();

                secondIteration.putAll(secondIterationPartial);
        }
        /*
        Map<String, Integer> fullResult = countFullResult(result, secondIteration, inputs.alternatives_ids); // ostateczny ranking z miejscami

        return fullResult;
        */

        return secondIteration;
    }

    private static Map<String, Integer> countPartialResults(Map<String, Double> mapTuples, InputsHandler.Inputs inputs, ProgramExecutionResult executionResult, int shift) {
        Map<String, Integer> results = new HashMap<>();

        if(mapTuples.size() == 1) {
            Map.Entry<String,Double> entry = mapTuples.entrySet().iterator().next();
            String key= entry.getKey();
            results.put(key, shift);
            return results;
        }

        ArrayList<Triple<String, String, Double>> partialWeights = getPartialWeights(mapTuples, inputs.preferenceTable);
        List<String> partialAlternatives = new ArrayList<>();

        for(String key : mapTuples.keySet())  {
            partialAlternatives.add(key);
        }

        Map<String, Double> iterationPartial = countResult(inputs.function, inputs.direction, partialAlternatives, partialWeights, executionResult);

        List<Map<String, Double>> sameValues = getSameValuesOnList(iterationPartial);

        ListMapsComparer listMapsComparer = new ListMapsComparer();
        Collections.sort(sameValues, listMapsComparer);

        int currentShift = shift;

        if(sameValues.size() == 1) {
            for(int i = 0; i < sameValues.get(0).size(); i++) {
                Map.Entry<String,Double> entry = sameValues.get(0).entrySet().iterator().next();
                String key= entry.getKey();
                results.put(key, shift);
            }
        }

        for(Map<String, Double> tuple: sameValues) {
            Map<String, Integer> partial = countPartialResults(tuple, inputs, executionResult, currentShift);
            currentShift += tuple.size();
            results.putAll(partial);
        }

        return results;
    }

    private static List<Map<String, Double>> getSameValuesOnList(Map<String, Double> result) {
        boolean existsValue = false;

        List<Map<String, Double>> sameValues = new ArrayList<>();
        for(String alternative: result.keySet()) {
            existsValue = false;
            for(Map<String, Double> same : sameValues) {
                if(same.containsValue(result.get(alternative).doubleValue())) { // jeżeli wartość result aktualnej alternative jest taka sama jak danego elementu na liście
                    same.put(alternative, result.get(alternative).doubleValue());
                    existsValue = true;
                    break;
                }
            }
            if(!existsValue) {
                Map<String, Double> nextValueAlternative = new HashMap<>();
                nextValueAlternative.put(alternative, result.get(alternative).doubleValue());
                sameValues.add(nextValueAlternative);
            }
        }

        return sameValues;
    }

    private static Map<String, Double> countResult (String function, String direction, List<String> alternatives, ArrayList<Triple<String, String, Double>> weightsTable, ProgramExecutionResult executionResult) {
        Map<String, Double> result = new LinkedHashMap<>();

        if("max".equalsIgnoreCase(function) && "in favor".equalsIgnoreCase(direction)) {
            result = max_in_favor(alternatives, weightsTable);
        } else if("min".equalsIgnoreCase(function) && "in favor".equalsIgnoreCase(direction)) {
            result = min_in_favor(alternatives, weightsTable);
        } else if("sum".equalsIgnoreCase(function) && "in favor".equalsIgnoreCase(direction)) {
            result = sum_in_favor(alternatives, weightsTable);
        } else if("max".equalsIgnoreCase(function) && "against".equalsIgnoreCase(direction)) {
            result = max_against(alternatives, weightsTable);
        } else if("min".equalsIgnoreCase(function) && "against".equalsIgnoreCase(direction)) {
            result = min_against(alternatives, weightsTable);
        } else if("sum".equalsIgnoreCase(function) && "against".equalsIgnoreCase(direction)) {
            result = sum_against(alternatives, weightsTable);
        } else if("max".equalsIgnoreCase(function) && "difference".equalsIgnoreCase(direction)) {
            result = max_difference(alternatives, weightsTable);
        } else if("min".equalsIgnoreCase(function) && "difference".equalsIgnoreCase(direction)) {
            result = min_difference(alternatives, weightsTable);
        } else if("sum".equalsIgnoreCase(function) && "difference".equalsIgnoreCase(direction)) {
            result = sum_difference(alternatives, weightsTable);
        } else {
            executionResult.addError("incorrect program parameters");
        }

        return result;
    }

    private static Map<String, Integer> countFullResult(Map<String, Double> firstIteration, Map<String, Double> secondIteration, List<String> alternatives) {

        List<Triple<String, Double, Double>> triples = new ArrayList<>();

        TripleComparer comparator = new TripleComparer();

        for(String alternative: alternatives) {
            double first = firstIteration.get(alternative).doubleValue();
            double second = -2;
            if(secondIteration.containsKey(alternative)) {
                second = secondIteration.get(alternative).doubleValue();
            }

            Triple<String, Double, Double> triple = new Triple<>(alternative, first, second);

            triples.add(triple);
        }

        Collections.sort(triples, comparator);

        Map<String, Integer> result = new HashMap<>();
        int place = 1;

        for(Triple<String, Double, Double> triple : triples) {
            int currentIndex = triples.indexOf(triple);

            result.put(triple.getFirst(), place);

            if(currentIndex < triples.size() - 1) {
                if (!((triples.get(currentIndex + 1).getSecond() == triples.get(currentIndex).getSecond()) && (triples.get(currentIndex + 1).getThird() == triples.get(currentIndex).getThird()))) {
                    place++;
                }
            }

        }

        return result;
    }

    private static ArrayList<Triple<String, String, Double>> getPartialWeights(Map<String, Double> sameValuesTuples, List<Triple<String, String, Double>> weightsTable) {

        ArrayList<Triple<String, String, Double>> partialWeights = new ArrayList<>();

        for(Triple<String, String, Double> tuple: weightsTable) {
            if(sameValuesTuples.containsKey(tuple.getFirst()) && sameValuesTuples.containsKey(tuple.getSecond())) {
                partialWeights.add(tuple);
            }
        }

        return partialWeights;
    }

    private static Map<String, Double> max_in_favor(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> max_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> row = getRow(weightsTable, alternative);
            double maxValue = Format(Max(row));
            max_table.put(alternative, maxValue);
        }

        return max_table;
    }

    private static Map<String, Double> min_in_favor(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> min_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> row = getRow(weightsTable, alternative);
            double minValue = Format(Min(row));
            min_table.put(alternative, minValue);
        }

        return min_table;
    }

    private static Map<String, Double> sum_in_favor(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> sum_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> row = getRow(weightsTable, alternative);
            double sumValue = Format(Sum(row));
            sum_table.put(alternative, sumValue);
        }

        return sum_table;
    }

    private static Map<String, Double> max_against(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> max_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(weightsTable, alternative);
            double maxValue = Format(-Max(column));
            max_table.put(alternative, maxValue);
        }

        return max_table;
    }

    private static Map<String, Double> min_against(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> min_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(weightsTable, alternative);
            double minValue = Format(-Min(column));
            min_table.put(alternative, minValue);
        }

        return min_table;
    }

    private static Map<String, Double> sum_against(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> sum_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(weightsTable, alternative);
            double sumValue = Format(-Sum(column));
            sum_table.put(alternative, sumValue);
        }

        return sum_table;
    }

    private static Map<String, Double> max_difference(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> max_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(weightsTable, alternative);
            ArrayList<Triple<String, String, Double>> row = getRow(weightsTable, alternative);

            ArrayList<Triple<String, String, Double>> diff = new ArrayList<>();
            for(Triple<String, String, Double> columnTriple: column) {
                for(Triple<String, String, Double> rowTriple: row) {
                    if(columnTriple.getFirst().equalsIgnoreCase(rowTriple.getSecond()) && columnTriple.getSecond().equalsIgnoreCase(rowTriple.getFirst())) {
                        double thirdInTriple = rowTriple.getThird() - columnTriple.getThird();
                        Triple<String, String, Double> newTriple = new Triple<>(columnTriple.getFirst(), columnTriple.getSecond(), thirdInTriple);
                        diff.add(newTriple);
                    }
                }
            }

            double maxValue = Format(Max(diff));
            max_table.put(alternative, maxValue);
        }
        return max_table;
    }

    private static Map<String, Double> min_difference(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> min_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(weightsTable, alternative);
            ArrayList<Triple<String, String, Double>> row = getRow(weightsTable, alternative);

            ArrayList<Triple<String, String, Double>> diff = new ArrayList<>();
            for(Triple<String, String, Double> columnTriple: column) {
                for(Triple<String, String, Double> rowTriple: row) {
                    if(columnTriple.getFirst().equalsIgnoreCase(rowTriple.getSecond()) && columnTriple.getSecond().equalsIgnoreCase(rowTriple.getFirst())) {
                        double thirdInTriple = rowTriple.getThird() - columnTriple.getThird();
                        Triple<String, String, Double> newTriple = new Triple<>(columnTriple.getFirst(), columnTriple.getSecond(), thirdInTriple);
                        diff.add(newTriple);
                    }
                }
            }

            double minValue = Format(Min(diff));
            min_table.put(alternative, minValue);
        }

        return min_table;
    }

    private static Map<String, Double> sum_difference(List<String> alternatives_ids, List<Triple<String, String, Double>> weightsTable) {

        Map<String, Double> sum_table = new LinkedHashMap<>();

        for (String alternative : alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(weightsTable, alternative);
            ArrayList<Triple<String, String, Double>> row = getRow(weightsTable, alternative);

            ArrayList<Triple<String, String, Double>> diff = new ArrayList<>();
            for(Triple<String, String, Double> columnTriple: column) {
                for(Triple<String, String, Double> rowTriple: row) {
                    if(columnTriple.getFirst().equalsIgnoreCase(rowTriple.getSecond()) && columnTriple.getSecond().equalsIgnoreCase(rowTriple.getFirst())) {
                        double thirdInTriple = rowTriple.getThird() - columnTriple.getThird();
                        Triple<String, String, Double> newTriple = new Triple<>(columnTriple.getFirst(), columnTriple.getSecond(), thirdInTriple);
                        diff.add(newTriple);
                    }
                }
            }

            double sumValue = Format(Sum(diff));
            sum_table.put(alternative, sumValue);
        }

        return sum_table;
    }

    private static ArrayList<Triple<String, String, Double>> getRow(List<Triple<String, String, Double>> weightsTable, String alternative) {

        ArrayList<Triple<String, String, Double>> row = new ArrayList<>();

        for(Triple<String, String, Double> tuple : weightsTable) {
            if(alternative.equalsIgnoreCase(tuple.getFirst()) && !alternative.equalsIgnoreCase(tuple.getSecond())) {
                row.add(tuple);
            }
        }

        return row;
    }

    private static ArrayList<Triple<String, String, Double>> getColumn(List<Triple<String, String, Double>> weightsTable, String alternative) {

        ArrayList<Triple<String, String, Double>> column = new ArrayList<>();

        for(Triple<String, String, Double> tuple : weightsTable) {
            if(alternative.equalsIgnoreCase(tuple.getSecond()) && !alternative.equalsIgnoreCase(tuple.getFirst())) {
                column.add(tuple);
            }
        }

        return column;
    }

    private static double Max(ArrayList<Triple<String, String, Double>> line) {

        ArrayList<Double> values = new ArrayList<>();
        for(Triple<String, String, Double> tuple : line) {
            values.add(tuple.getThird());
        }

        double max = Collections.max(values);
        return max;
    }

    private static double Min(ArrayList<Triple<String, String, Double>> line) {

        ArrayList<Double> values = new ArrayList<>();
        for(Triple<String, String, Double> tuple : line) {
            values.add(tuple.getThird());
        }

        double min = Collections.min(values);
        return min;
    }

    private static double Sum(ArrayList<Triple<String, String, Double>> line) {

        double sum = 0;
        for(Triple<String, String, Double> tuple : line) {
            sum += tuple.getThird();
        }

        return sum;
    }

    private static double Format(double number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
