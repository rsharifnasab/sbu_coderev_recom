package ir.msdehghan.revrec.framework.feature.engineering.experience;

import ir.msdehghan.revrec.framework.feature.engineering.FeatureExtractor;
import ir.msdehghan.revrec.framework.feature.engineering.time.LastReviewsState;
import ir.msdehghan.revrec.framework.model.Review;
import ir.msdehghan.revrec.framework.recommendation.revfinder.FilePathSimilarity;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.time.Duration;
import java.util.*;

public class FPScore implements FeatureExtractor {
    private static final long WEEK_IN_MILLIS = Duration.ofDays(7).toMillis();
    private static final MutableDouble ZERO = new MutableDouble(0);
    private final List<ExperienceColumn> columns;
    private final LastReviewsState state;
    private Review currentReview;
    private long currentReviewTimestamp;

    public FPScore(LastReviewsState state) {
        this.state = state;
        FilePathSimilarity.clearCache();
        this.columns = List.of(
                new ExperienceColumn(2*WEEK_IN_MILLIS, "FP2w"),
                new ExperienceColumn(4*WEEK_IN_MILLIS, "FP4w"),
                new ExperienceColumn(6*WEEK_IN_MILLIS, "FP6w"),
                new ExperienceColumn(8*WEEK_IN_MILLIS, "FP8w")
        );
    }

    @Override
    public List<String> getHeaders() {
        return columns.stream().map(c -> c.columnName).toList();
    }

    @Override
    public void setTargetReview(Review review) {
        this.currentReview = review;
        this.currentReviewTimestamp = review.getSubmitTimestamp();
        calculateExperienceMap();
    }

    @Override
    public void addColumnValues(Map<String, Object> row, String person) {
        for (ExperienceColumn column : columns) {
            row.put(column.columnName, column.experiemceMap.getOrDefault(person, ZERO).doubleValue());
        }
    }

    private void calculateExperienceMap() {
        columns.forEach(c -> c.experiemceMap.clear());
        for (Review pastReview : state.getReviews()) {
            Map<String, MutableDouble> experienceMap = null;
            final long pastReviewTimestamp = pastReview.getSubmitTimestamp();
            final long ageMillis = currentReviewTimestamp - pastReviewTimestamp;
            for (ExperienceColumn column : columns) {
                if (ageMillis <= column.maxAgeMillis) {
                    experienceMap = column.experiemceMap;
                    break;
                }
            }
            if (experienceMap == null) {
                continue;
            }

            final double rawSimilarity = calculateSimilarity(pastReview, currentReview);
            if (rawSimilarity == 0.0) continue;
            for (String reviewer : pastReview.getReviewers()) {
                experienceMap.computeIfAbsent(reviewer, s -> new MutableDouble()).add(rawSimilarity);
            }
        }
    }

    private static double calculateSimilarity(Review pastReview, Review newReview) {
        double score = 0;
        final var pastReviewFilePaths = pastReview.getFilePaths();
        for (String f1 : newReview.getFilePaths()) {
            for (String f2 : pastReviewFilePaths) {
                score += FilePathSimilarity.longestCommonPrefix(f1, f2);
            }
        }
        return score / (pastReviewFilePaths.size() * newReview.getFilePaths().size());
    }

    public record ExperienceColumn(long maxAgeMillis, String columnName, Map<String, MutableDouble> experiemceMap) {

        public ExperienceColumn(long maxAgeMillis, String columnName) {
            this(maxAgeMillis, columnName, new HashMap<>(200));
        }
    }
}
