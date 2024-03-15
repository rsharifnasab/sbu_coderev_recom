package ir.msdehghan.revrec.framework.metrics;


import ir.msdehghan.revrec.framework.model.RecommendationResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TopK implements EvaluationMetric<RecommendationResult> {
    private final int maxK;
    private final Map<Integer, Double> sumOfTopK = new HashMap<>();
    private int totalCount;

    public TopK(int maxK) {
        this.maxK = maxK;
        IntStream.range(1, maxK + 1).forEach(i -> sumOfTopK.put(i, 0.0));
    }

    @Override
    public void accept(@NotNull RecommendationResult result) {
        totalCount++;
        int firstRank = result.getActual().stream()
                .mapToInt(result.getRecommended()::indexOf)
                .filter(i -> i >= 0)
                .map(i -> i+1)
                .min().orElse(Integer.MAX_VALUE);
        for (int k = 1; k <= maxK; k++) {
            if (firstRank <= k) {
                sumOfTopK.compute(k, (i, v) -> v + 1);
            }
        }
    }

    @Override
    public Map<Integer, Double> getResult() {
        Map<Integer, Double> result = new HashMap<>(maxK);
        for (Map.Entry<Integer, Double> entry : sumOfTopK.entrySet()) {
            result.put(entry.getKey(), 100 * entry.getValue() / totalCount);
        }
        return result;
    }
}
