package ir.msdehghan.revrec.framework.feature.engineering;

import ir.msdehghan.revrec.framework.model.Review;

import java.util.List;
import java.util.Map;

public class IsReviewerColumn implements FeatureExtractor {

    public static final String LABEL_COLUMN = "is_reviewer";
    private Review currentReview;

    @Override
    public void setTargetReview(Review review) {
        currentReview = review;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(LABEL_COLUMN);
    }

    @Override
    public void addColumnValues(Map<String, Object> row, String person) {
        row.put(LABEL_COLUMN, currentReview.getReviewers().contains(person) ? "1" : "0");
    }
}
