package ir.msdehghan.rm.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class PrecisionAtK implements EvaluationMetric<RecommendationResult> {
    private final int maxK;
    private final Map<Integer, Double> sumOfPrecisionAtK = new HashMap<>();
    private int totalCount;

    public PrecisionAtK(int maxK) {
        this.maxK = maxK;
        IntStream.range(1, maxK + 1).forEach(i -> sumOfPrecisionAtK.put(i, 0.0));
    }

    @Override
    public void accept(RecommendationResult result) {
        final int recommendedCount = result.getRecommended().size();

        totalCount++;
        int actualRecommendedCount = 0;
        for (int k = 1; k <= maxK; k++) {
            if (k <= recommendedCount && result.getActual().contains(result.getRecommended().get(k - 1))) {
                actualRecommendedCount++;
            }

            double precision = sumOfPrecisionAtK.get(k) + 100 * (actualRecommendedCount / (k + 0.0));
            sumOfPrecisionAtK.put(k, precision);
        }
    }

    @Override
    public Map<Integer, Double> getResult() {
        Map<Integer, Double> result = new HashMap<>(maxK);
        for (Map.Entry<Integer, Double> entry : sumOfPrecisionAtK.entrySet()) {
            result.put(entry.getKey(), entry.getValue() / totalCount);
        }
        return result;
    }
}
