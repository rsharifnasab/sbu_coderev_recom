package ir.msdehghan.rm.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class MRR implements EvaluationMetric<RecommendationResult> {
    private final int maxK;
    private final Map<Integer, Double> sumOfMrrAtK = new HashMap<>();
    private int totalCount;

    public MRR(int maxK) {
        this.maxK = maxK;
        IntStream.range(1, maxK + 1).forEach(i -> sumOfMrrAtK.put(i, 0.0));
    }

    @Override
    public void accept(RecommendationResult result) {
        totalCount++;
        int firstRank = result.getActual().stream()
                .mapToInt(result.getRecommended()::indexOf)
                .filter(i -> i >= 0)
                .map(i -> i+1)
                .min().orElse(Integer.MAX_VALUE);
        for (int k = 1; k <= maxK; k++) {
            if (firstRank <= k) {
                double sum = sumOfMrrAtK.get(k) + 100 * (1.0 / firstRank);
                sumOfMrrAtK.put(k, sum);
            }
        }
    }

    @Override
    public Map<Integer, Double> getResult() {
        Map<Integer, Double> result = new HashMap<>(maxK);
        for (Map.Entry<Integer, Double> entry : sumOfMrrAtK.entrySet()) {
            result.put(entry.getKey(), entry.getValue() / totalCount);
        }
        return result;
    }
}
