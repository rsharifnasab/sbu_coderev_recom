package ir.msdehghan.revrec.framework.recommendation;

import ir.msdehghan.revrec.framework.model.RecommendationResult;
import ir.msdehghan.revrec.framework.model.Review;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MostActive implements RecommendationMethod {
    private final Map<String, Integer> history = new HashMap<>(200);
    private final int k;

    public MostActive(int k) {
        this.k = k;
    }

    @Override
    public RecommendationResult recommendReviewers(Review review) {
        List<String> recommended = history.entrySet().stream()
                .filter(e -> e.getValue() != 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(k)
                .toList();
        return new RecommendationResult(review.getId(), review.getReviewers(), recommended,
                Map.of("activity", Map.copyOf(history)));
    }

    @Override
    public List<String> getMetadataNames() {
        return List.of("activity");
    }

    @Override
    public void addToState(List<Review> reviews) {
        for (Review review : reviews) {
            for (String reviewer : review.getReviewers()) {
                history.compute(reviewer, (name, score) -> score == null ? 1 : score + 1);
            }
        }
    }

    @Override
    public void removeFromState(List<Review> reviews) {
        for (Review review : reviews) {
            for (String reviewer : review.getReviewers()) {
                history.computeIfPresent(reviewer, (name, score) -> score == 1 ? null : score - 1);
            }
        }
    }
}
