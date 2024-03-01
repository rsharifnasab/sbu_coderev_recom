package ir.msdehghan.rm.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class RecallAtK implements EvaluationMetric<RecommendationResult> {
    private final int maxK;
    private final Map<Integer, Double> sumOfRecallAtK = new HashMap<>();
    private int totalCount;

    public RecallAtK(int maxK) {
        this.maxK = maxK;
        IntStream.range(1, maxK + 1).forEach(i -> sumOfRecallAtK.put(i, 0.0));
    }

    @Override
    public void accept(RecommendationResult result) {
        final int recommendedCount = result.getRecommended().size();

        totalCount++;
        int actualRecommendedCount = 0;
        final int actualCount = result.getActual().size();
        for (int k = 1; k <= maxK; k++) {
            if (k <= recommendedCount && result.getActual().contains(result.getRecommended().get(k - 1))) {
                actualRecommendedCount++;
            }

            double recall = sumOfRecallAtK.get(k) + 100 * (actualRecommendedCount / (actualCount + 0.0));
            sumOfRecallAtK.put(k, recall);
        }
    }

    @Override
    public Map<Integer, Double> getResult() {
        Map<Integer, Double> result = new HashMap<>(maxK);
        for (Map.Entry<Integer, Double> entry : sumOfRecallAtK.entrySet()) {
            result.put(entry.getKey(), entry.getValue() / totalCount);
        }
        return result;
    }
}
