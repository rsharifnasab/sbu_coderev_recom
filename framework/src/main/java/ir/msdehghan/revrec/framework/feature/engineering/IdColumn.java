package ir.msdehghan.revrec.framework.feature.engineering;

import ir.msdehghan.revrec.framework.model.Review;

import java.util.List;
import java.util.Map;

public class IdColumn implements FeatureExtractor {

    public static final String ID_COLUMN = "ID";
    public static final String PERSON_COLUMN = "person_id";
    public static final String PR_ID_COLUMN = "pr_id";
    public static final String IS_OWNER_COLUMN = "is_owner";
    private Review currentReview;

    @Override
    public void setTargetReview(Review review) {
        currentReview = review;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(ID_COLUMN, PR_ID_COLUMN, PERSON_COLUMN, IS_OWNER_COLUMN);
    }

    @Override
    public void addColumnValues(Map<String, Object> row, String person) {
        row.put(ID_COLUMN, currentReview.getId() + '-' + person);
        row.put(PR_ID_COLUMN, currentReview.getId());
        row.put(PERSON_COLUMN, person);
        row.put(IS_OWNER_COLUMN, currentReview.getOwner().equals(person) ? "1" : "0");
    }
}
