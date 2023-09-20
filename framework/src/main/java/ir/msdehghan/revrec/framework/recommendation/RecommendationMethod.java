package ir.msdehghan.revrec.framework.recommendation;

import ir.msdehghan.revrec.framework.model.RecommendationResult;
import ir.msdehghan.revrec.framework.model.Review;

import java.util.List;

public interface RecommendationMethod {
    RecommendationResult recommendReviewers(Review review);

    default void addToState(List<Review> reviews) {
        // Each method may implement these methods based on its needs.
    }

    default void removeFromState(List<Review> reviews) {
        // Each method may implement these methods based on its needs.
    }

    default void setPastReviews(List<Review> reviews) {
        // Each method may implement these methods based on its needs.
    }

    default List<String> getMetadataNames() {
        return List.of();
    }
}
