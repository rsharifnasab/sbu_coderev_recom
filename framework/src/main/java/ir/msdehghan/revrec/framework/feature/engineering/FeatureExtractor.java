package ir.msdehghan.revrec.framework.feature.engineering;

import ir.msdehghan.revrec.framework.model.Review;

import java.util.List;
import java.util.Map;

public interface FeatureExtractor {
    void setTargetReview(Review review);
    List<String> getHeaders();
    void addColumnValues(Map<String, Object> row, String person);
}
