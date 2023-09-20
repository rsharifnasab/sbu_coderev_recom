package ir.msdehghan.revrec.framework.feature.engineering.time;

import ir.msdehghan.revrec.framework.feature.engineering.FeatureExtractor;
import ir.msdehghan.revrec.framework.model.Review;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class TimeSinceBase implements FeatureExtractor {

    private static final long DAY_IN_MILLIS = Duration.ofDays(1).toMillis();
    public static final String DAYS_ELAPSED_HEADER = "DaysElapsed";
    private final long baseTime;
    private long days;

    public TimeSinceBase(long baseTime) {
        this.baseTime = baseTime;
    }

    @Override
    public void setTargetReview(Review review) {
        days = (review.getSubmitTimestamp() - baseTime) / DAY_IN_MILLIS;
    }

    @Override
    public List<String> getHeaders() {
        return List.of(DAYS_ELAPSED_HEADER);
    }

    @Override
    public void addColumnValues(Map<String, Object> row, String person) {
        row.put(DAYS_ELAPSED_HEADER, Long.toString(days));
    }
}
