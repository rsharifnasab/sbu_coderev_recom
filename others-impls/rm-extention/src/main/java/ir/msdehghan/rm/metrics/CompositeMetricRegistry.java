package ir.msdehghan.rm.metrics;

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
    public void accept(RecommendationResult result) {
        metrics.forEach(metric -> metric.accept(result));
    }

    @Override
    public Map<Integer, Double> getResult() {
        throw new UnsupportedOperationException();
    }

    public List<double[]> getResultAsRows() {
        List<double[]> rows = new ArrayList<>();
        for (int i = 1; i <= k; i++) {
            double[] row = new double[metrics.size() + 1]; // metrics + k header
            row[0] = i; // k
            for (int j = 1; j <= metrics.size(); j++) {
                row[j] = metrics.get(j - 1).getResult().get(i);
            }
            rows.add(row);
        }
        return rows;
    }

    public List<String> getHeaders() {
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
