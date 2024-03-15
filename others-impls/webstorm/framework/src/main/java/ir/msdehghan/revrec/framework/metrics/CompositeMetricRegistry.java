package ir.msdehghan.revrec.framework.metrics;


import ir.msdehghan.revrec.framework.model.RecommendationResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeMetricRegistry implements EvaluationMetric<RecommendationResult> {
    private final List<EvaluationMetric<RecommendationResult>> metrics;
    private final int k;

    @SafeVarargs
    public CompositeMetricRegistry(int k, IntFunction<EvaluationMetric<RecommendationResult>>... metricGenerators) {
        this.k = k;
        this.metrics = Stream.of(metricGenerators)
                .map(g -> g.apply(k)).collect(Collectors.toList());
    }

    @Override
    public void accept(@NotNull RecommendationResult result) {
        metrics.forEach(metric -> metric.accept(result));
    }

    @Override
    public Map<Integer, Double> getResult() {
        throw new UnsupportedOperationException();
    }

    public void printResult() {
        System.out.println(metrics.stream()
                .map(m -> m.getClass().getSimpleName())
                .collect(Collectors.joining("    ", "    ", "")));
        for (int i = 1; i <= k; i++) {
            System.out.printf("%2d  ", i);
            StringJoiner joiner = new StringJoiner("    ");
            for (EvaluationMetric<RecommendationResult> metric : metrics) {
                int nameLength = metric.getClass().getSimpleName().length();
                String format = String.format("%" + nameLength + ".2f", metric.getResult().get(i));
                joiner.add(format);
            }
            System.out.println(joiner);
        }
    }

    public List<Map<String,String>> getResultAsRows() {
        List<Map<String,String>> rows = new ArrayList<>();
        for (int i = 1; i <= k; i++) {
            Map<String, String> row = new HashMap<>();
            row.put("k", String.valueOf(i));
            for (EvaluationMetric<RecommendationResult> metric : metrics) {
                String metricName = metric.getClass().getSimpleName();
                row.put(metricName, String.format("%.2f", metric.getResult().get(i)));
            }
            rows.add(row);
        }
        return rows;
    }

    public List<String> getCsvHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("k");
        headers.addAll(metrics.stream()
                .map(m -> m.getClass().getSimpleName())
                .collect(Collectors.toList()));
        return headers;
    }

    public static CompositeMetricRegistry full(int k) {
        return new CompositeMetricRegistry(k, PrecisionAtK::new, RecallAtK::new, TopK::new, MRR::new, MAP::new);
    }
}
