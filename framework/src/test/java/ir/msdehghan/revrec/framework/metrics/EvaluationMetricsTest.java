package ir.msdehghan.revrec.framework.metrics;

import ir.msdehghan.revrec.framework.model.RecommendationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluationMetricsTest {
    @Test
    void topKTest() {
        EvaluationMetric<RecommendationResult> topK = new TopK(3);
        topK.accept(new RecommendationResult(List.of(1, 2), List.of(3)));
        assertEquals(Map.of(1, 0.0, 2, 0.0, 3, 0.0), topK.getResult());

        topK.accept(new RecommendationResult(List.of(1, 2, 3), List.of(4, 2)));
        assertEquals(Map.of(1, 0.0, 2, 50.0, 3, 50.0), topK.getResult());

        topK.accept(new RecommendationResult(List.of(1, 2, 3), List.of(3, 2, 1)));
        topK.accept(new RecommendationResult(List.of(1, 2, 3), List.of(1, 2, 3, 4, 5)));
        assertEquals(Map.of(1, 50.0, 2, 75.0, 3, 75.0), topK.getResult());

        topK.accept(new RecommendationResult(List.of(1), List.of()));
        assertEquals(Map.of(1, 40.0, 2, 60.0, 3, 60.0), topK.getResult());
    }

    @Test
    void recallAtKTest() {
        EvaluationMetric<RecommendationResult> recallAtK = new RecallAtK(3);
        recallAtK.accept(new RecommendationResult(List.of(1, 2), List.of(3)));
        assertEquals(Map.of(1, 0.0, 2, 0.0, 3, 0.0), recallAtK.getResult());

        recallAtK.accept(new RecommendationResult(List.of(1, 2), List.of(4, 2, 1)));
        assertEquals(Map.of(1, 0.0, 2, 25.0, 3, 50.0), recallAtK.getResult());

        recallAtK.accept(new RecommendationResult(List.of(1, 2), List.of(2, 1, 3)));
        recallAtK.accept(new RecommendationResult(List.of(1, 2), List.of(1, 2, 3)));
        assertEquals(Map.of(1, 25.0, 2, 62.5, 3, 75.0), recallAtK.getResult());

        recallAtK.accept(new RecommendationResult(List.of(1), List.of()));
        assertEquals(Map.of(1, 20.0, 2, 50.0, 3, 60.0), recallAtK.getResult());

        recallAtK = new RecallAtK(10);
        recallAtK.accept(new RecommendationResult(
                List.of(11, 12, 13, 14, 15, 16),
                List.of(11, 1, 12, 13, 14, 15, 2, 3, 4, 16)));
        assertEquals(100.0, round(recallAtK.getResult().get(10)));
        assertEquals(50.0, round(recallAtK.getResult().get(4)));

        recallAtK.accept(new RecommendationResult(
                List.of(11, 12, 13, 14, 15, 16),
                List.of(1, 11, 2, 3, 12, 13, 14, 4, 15, 16)));
        assertEquals(100.0, round(recallAtK.getResult().get(10)));
        assertEquals(33.33, round(recallAtK.getResult().get(4)));
    }

    @Test
    void precisionAtKTest() {
        EvaluationMetric<RecommendationResult> precisionAtK = new PrecisionAtK(2);
        precisionAtK.accept(new RecommendationResult(List.of(1, 2), List.of(3)));
        assertEquals(Map.of(1, 0.0, 2, 0.0), precisionAtK.getResult());

        precisionAtK.accept(new RecommendationResult(List.of(1, 2, 3), List.of(4, 2)));
        assertEquals(Map.of(1, 0.0, 2, 25.0), precisionAtK.getResult());

        precisionAtK.accept(new RecommendationResult(List.of(1, 2, 3), List.of(3, 2, 1)));
        precisionAtK.accept(new RecommendationResult(List.of(1, 2, 3), List.of(1, 2, 3)));
        assertEquals(Map.of(1, 50.0, 2, 62.5), precisionAtK.getResult());

        precisionAtK.accept(new RecommendationResult(List.of(1), List.of()));
        assertEquals(Map.of(1, 40.0, 2, 50.0), precisionAtK.getResult());

        precisionAtK = new PrecisionAtK(10);
        precisionAtK.accept(new RecommendationResult(
                List.of(11, 12, 13, 14, 15, 16),
                List.of(11, 1, 12, 13, 14, 15, 2, 3, 4, 16)));
        assertEquals(60.0, round(precisionAtK.getResult().get(10)));
        assertEquals(75.0, round(precisionAtK.getResult().get(4)));

        precisionAtK.accept(new RecommendationResult(
                List.of(11, 12, 13, 14, 15, 16),
                List.of(1, 11, 2, 3, 12, 13, 14, 4, 15, 16)));
        assertEquals(60.0, round(precisionAtK.getResult().get(10)));
        assertEquals(50.0, round(precisionAtK.getResult().get(4)));
    }

    @Test
    void MrrAtKTest() {
        EvaluationMetric<RecommendationResult> mrr = new MRR(3);
        mrr.accept(new RecommendationResult(List.of(1, 2), List.of(3)));
        assertEquals(Map.of(1, 0.0, 2, 0.0, 3, 0.0), mrr.getResult());

        mrr.accept(new RecommendationResult(List.of(1, 2, 3), List.of(4, 2, 1)));
        assertEquals(Map.of(1, 0.0, 2, 25.0, 3, 25.0), mrr.getResult());

        mrr.accept(new RecommendationResult(List.of(1, 2, 3), List.of(3, 2, 1)));
        mrr.accept(new RecommendationResult(List.of(1, 2, 3), List.of(1, 2, 3, 4, 5)));
        assertEquals(Map.of(1, 50.0, 2, 62.5, 3, 62.5), mrr.getResult());

        mrr.accept(new RecommendationResult(List.of(1), List.of()));
        assertEquals(Map.of(1, 40.0, 2, 50.0, 3, 50.0), mrr.getResult());
    }

    @Test
    void MapAtKTest() {
        EvaluationMetric<RecommendationResult> map = new MAP(10);
        map.accept(new RecommendationResult(
                List.of(11, 12, 13, 14, 15, 16),
                List.of(11, 1, 12, 13, 14, 15, 2, 3, 4, 16)));
        assertEquals(77.5, round(map.getResult().get(10)));
        assertEquals(27.78, round(map.getResult().get(3)));

        map.accept(new RecommendationResult(
                List.of(11, 12, 13, 14, 15, 16),
                List.of(1, 11, 2, 3, 12, 13, 14, 4, 15, 16)));
        assertEquals(64.81, round(map.getResult().get(10)));
        assertEquals(18.06, round(map.getResult().get(3)));
    }

    public static double round(double d) {
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}