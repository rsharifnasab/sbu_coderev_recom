package ir.msdehghan.revrec.framework.recommendation.fcg;

import ir.msdehghan.revrec.framework.model.RecommendationResult;
import ir.msdehghan.revrec.framework.model.Review;
import ir.msdehghan.revrec.framework.recommendation.RecommendationMethod;
import ir.msdehghan.revrec.framework.recommendation.fcg.graph.FileCoOccurrenceGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class FCG implements RecommendationMethod {
    private static final Logger logger = LogManager.getLogger(FCG.class);

    private final FileCoOccurrenceGraph graph;
    private List<Review> pastReviews;
    private final int k;

    public FCG(int k) {
        this.k = k;
        graph = new FileCoOccurrenceGraph();
    }

    @Override
    public RecommendationResult recommendReviewers(Review review) {
        Map<String, Double> scoreMap = new HashMap<>(100);
        for (Review pastReview : pastReviews) {
            var similarity = calculateSimilarity(pastReview, review);
            if (similarity == 0.0) continue;
            for (String reviewer : pastReview.getReviewers()) {
                scoreMap.compute(reviewer, (r, score) -> score == null ? similarity : score + similarity);
            }
//            scoreMap.compute(pastReview.getOwner(), (r, score) -> score == null ? similarity : score + similarity);
        }
        List<String> recommended = scoreMap.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Double>>comparingDouble(Map.Entry::getValue).reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableList());
        return new RecommendationResult(review.getId(), review.getReviewers(), recommended);
    }

    private double calculateSimilarity(Review pastReview, Review newReview) {
        double score = 0;
        for (String f1 : newReview.getFilePaths()) {
            if (!graph.containsVertex(f1)) continue;
            for (String f2 : pastReview.getFilePaths()) {
                if (!graph.containsVertex(f2)) continue;
                score += 1.0 / (1 + graph.getDistance(f1, f2));
            }
        }
        return score / (pastReview.getFilePaths().size() * newReview.getFilePaths().size());
    }

    @Override
    public void addToState(List<Review> reviews) {
        graph.addReviews(reviews);
    }

    @Override
    public void removeFromState(List<Review> reviews) {
        graph.deleteReviews(reviews);
    }

    @Override
    public void setPastReviews(List<Review> reviews) {
        this.pastReviews = reviews;
    }
}
