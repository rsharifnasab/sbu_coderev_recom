package ir.msdehghan.rm.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class MAP implements EvaluationMetric<RecommendationResult> {
    private final int maxK;
    private final Map<Integer, Double> sumOfAveragePrecisionAtK = new HashMap<>();
    private int reviewCounter;

    public MAP(int maxK) {
        this.maxK = maxK;
        IntStream.range(1, maxK + 1).forEach(i -> sumOfAveragePrecisionAtK.put(i, 0.0));
    }

    @Override
    public void accept(RecommendationResult result) {
        final int recommendedCount = result.getRecommended().size();

        reviewCounter++;
        double averagePrecision = 0;
        int actualRecommendedCount = 0;
        for (int k = 1; k <= maxK; k++) {
            if (k <= recommendedCount && result.getActual().contains(result.getRecommended().get(k - 1))) {
                actualRecommendedCount++;
                averagePrecision += (actualRecommendedCount + 0.0) / k;
            }

            double precision = sumOfAveragePrecisionAtK.get(k) + (100 * averagePrecision / result.getActual().size());
            sumOfAveragePrecisionAtK.put(k, precision);
        }
    }

    @Override
    public Map<Integer, Double> getResult() {
        Map<Integer, Double> result = new HashMap<>(maxK);
        for (Map.Entry<Integer, Double> entry : sumOfAveragePrecisionAtK.entrySet()) {
            result.put(entry.getKey(), entry.getValue() / reviewCounter);
        }
        return result;
    }
}
