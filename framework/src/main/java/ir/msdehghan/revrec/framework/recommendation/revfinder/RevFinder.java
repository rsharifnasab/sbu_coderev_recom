package ir.msdehghan.revrec.framework.recommendation.revfinder;

import ir.msdehghan.revrec.framework.model.RecommendationResult;
import ir.msdehghan.revrec.framework.model.Review;
import ir.msdehghan.revrec.framework.recommendation.RecommendationMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleBiFunction;

public class RevFinder implements RecommendationMethod {
    private List<Review> pastReviews;
    private final int k;

    public RevFinder(int k) {
        this.k = k;
        FilePathSimilarity.clearCache();
    }

    @Override
    public void setPastReviews(List<Review> reviews) {
        this.pastReviews = reviews;
    }

    @Override
    public RecommendationResult recommendReviewers(Review review) {
        long start = System.currentTimeMillis();

        var longestCommonPrefix = recommendReviewers(review, FilePathSimilarity::longestCommonPrefix);
        var longestCommonSuffix = recommendReviewers(review, FilePathSimilarity::longestCommonSuffix);
        var longestCommonSubsequence = recommendReviewers(review, FilePathSimilarity::longestCommonSubsequence);
        var longestCommonSubstring = recommendReviewers(review, FilePathSimilarity::longestCommonSubstring);

        var scoreMap = bordaCount(List.of(longestCommonPrefix, longestCommonSuffix,
                longestCommonSubsequence, longestCommonSubstring));
        List<String> recommended = scoreMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(k)
                .toList();
        long elapsed = System.currentTimeMillis() - start;
        return new RecommendationResult(review.getId(), review.getReviewers(), recommended, elapsed);
        /*return new RecommendationResult(review.getId(), review.getReviewers(), recommended, Map.of(
                "prefix", longestCommonPrefix,
                "suffix", longestCommonSuffix,
                "subsequence", longestCommonSubsequence,
                "substring", longestCommonSubstring,
                "finalScore", scoreMap
        ));*/
    }

    @Override
    public List<String> getMetadataNames() {
        return List.of("prefix","suffix","subsequence","substring", "finalScore");
    }

    private Map<String, Integer> bordaCount(List<Map<String, Double>> results) {
        Map<String, Integer> scoreMap = new HashMap<>(results.get(0).size());
        for (var resultMap : results) {
            List<String> result = extractListFromScoreMap(resultMap);
            int size = result.size();
            for (int i = 0; i < size; i++) {
                int score = size - i - 1;
                scoreMap.compute(result.get(i), (r, v) -> v == null ? score : v + score);
            }
        }
        return scoreMap;
    }

    private Map<String, Double> recommendReviewers(Review review, ToDoubleBiFunction<String, String> similarityFunction) {
        Map<String, Double> scoreMap = new HashMap<>(100);
        for (Review pastReview : pastReviews) {
            var similarity = calculateSimilarity(pastReview, review, similarityFunction);
            if (similarity == 0.0) continue;
            for (String reviewer : pastReview.getReviewers()) {
                scoreMap.compute(reviewer, (r, score) -> score == null ? similarity : score + similarity);
            }
        }
        return scoreMap;
    }

    @NotNull
    private static List<String> extractListFromScoreMap(Map<String, Double> scoreMap) {
        return scoreMap.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .toList();
    }

    private double calculateSimilarity(Review pastReview, Review newReview,
                                       ToDoubleBiFunction<String, String> similarityFunction) {
        double score = 0;
        for (String f1 : newReview.getFilePaths()) {
            for (String f2 : pastReview.getFilePaths()) {
                score += similarityFunction.applyAsDouble(f1, f2);
            }
        }
        return score / (pastReview.getFilePaths().size() * newReview.getFilePaths().size());
    }
}