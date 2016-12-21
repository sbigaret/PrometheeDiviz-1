package org.NetFlowScore;

import org.NetFlowScore.structures.Triple;
import org.NetFlowScore.xmcda.InputsHandler;
import org.xmcda.ProgramExecutionResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class NetFlowScore {
    public static Map<String, Double> calculateNetFlowScore(InputsHandler.Inputs inputs, ProgramExecutionResult executionResult) {

        Map<String, Double> result = new LinkedHashMap<>();

        if("max".equalsIgnoreCase(inputs.function) && "in favor".equalsIgnoreCase(inputs.direction)) {
            result = max_in_favor(inputs);
        } else if("min".equalsIgnoreCase(inputs.function) && "in favor".equalsIgnoreCase(inputs.direction)) {
            result = min_in_favor(inputs);
        } else if("sum".equalsIgnoreCase(inputs.function) && "in favor".equalsIgnoreCase(inputs.direction)) {
            result = sum_in_favor(inputs);
        } else if("max".equalsIgnoreCase(inputs.function) && "against".equalsIgnoreCase(inputs.direction)) {
            result = max_against(inputs);
        } else if("min".equalsIgnoreCase(inputs.function) && "against".equalsIgnoreCase(inputs.direction)) {
            result = min_against(inputs);
        } else if("sum".equalsIgnoreCase(inputs.function) && "against".equalsIgnoreCase(inputs.direction)) {
            result = sum_against(inputs);
        } else if("max".equalsIgnoreCase(inputs.function) && "difference".equalsIgnoreCase(inputs.direction)) {
            result = max_difference(inputs);
        } else if("min".equalsIgnoreCase(inputs.function) && "difference".equalsIgnoreCase(inputs.direction)) {
            result = min_difference(inputs);
        } else if("sum".equalsIgnoreCase(inputs.function) && "difference".equalsIgnoreCase(inputs.direction)) {
            result = sum_difference(inputs);
        } else {
            executionResult.addError("incorrect program parameters");
        }


        return result;
    }


    private static Map<String, Double> max_in_favor(InputsHandler.Inputs inputs) {

        Map<String, Double> max_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> row = getRow(inputs, alternative);
            double maxValue = Format(Max(row));
            max_table.put(alternative, maxValue);
        }

        return max_table;
    }

    private static Map<String, Double> min_in_favor(InputsHandler.Inputs inputs) {

        Map<String, Double> min_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> row = getRow(inputs, alternative);
            double minValue = Format(Min(row));
            min_table.put(alternative, minValue);
        }

        return min_table;
    }

    private static Map<String, Double> sum_in_favor(InputsHandler.Inputs inputs) {

        Map<String, Double> sum_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> row = getRow(inputs, alternative);
            double sumValue = Format(Sum(row));
            sum_table.put(alternative, sumValue);
        }

        return sum_table;
    }

    private static Map<String, Double> max_against(InputsHandler.Inputs inputs) {

        Map<String, Double> max_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(inputs, alternative);
            double maxValue = Format(-Max(column));
            max_table.put(alternative, maxValue);
        }

        return max_table;
    }

    private static Map<String, Double> min_against(InputsHandler.Inputs inputs) {

        Map<String, Double> min_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(inputs, alternative);
            double minValue = Format(-Min(column));
            min_table.put(alternative, minValue);
        }

        return min_table;
    }

    private static Map<String, Double> sum_against(InputsHandler.Inputs inputs) {

        Map<String, Double> sum_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(inputs, alternative);
            double sumValue = Format(-Sum(column));
            sum_table.put(alternative, sumValue);
        }

        return sum_table;
    }

    private static Map<String, Double> max_difference(InputsHandler.Inputs inputs) {

        Map<String, Double> max_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(inputs, alternative);
            ArrayList<Triple<String, String, Double>> row = getRow(inputs, alternative);

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

    private static Map<String, Double> min_difference(InputsHandler.Inputs inputs) {

        Map<String, Double> min_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(inputs, alternative);
            ArrayList<Triple<String, String, Double>> row = getRow(inputs, alternative);

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

    private static Map<String, Double> sum_difference(InputsHandler.Inputs inputs) {

        Map<String, Double> sum_table = new LinkedHashMap<>();

        for (String alternative : inputs.alternatives_ids) {
            ArrayList<Triple<String, String, Double>> column = getColumn(inputs, alternative);
            ArrayList<Triple<String, String, Double>> row = getRow(inputs, alternative);

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

    private static ArrayList<Triple<String, String, Double>> getRow(InputsHandler.Inputs inputs, String alternative) {

        ArrayList<Triple<String, String, Double>> row = new ArrayList<>();

        for(Triple<String, String, Double> tuple : inputs.preferenceTable) {
            if(alternative.equalsIgnoreCase(tuple.getFirst()) && !alternative.equalsIgnoreCase(tuple.getSecond())) {
                row.add(tuple);
            }
        }

        return row;
    }

    private static ArrayList<Triple<String, String, Double>> getColumn(InputsHandler.Inputs inputs, String alternative) {

        ArrayList<Triple<String, String, Double>> column = new ArrayList<>();

        for(Triple<String, String, Double> tuple : inputs.preferenceTable) {
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

