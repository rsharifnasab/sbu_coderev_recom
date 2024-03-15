package ir.msdehghan.revrec.framework.feature.engineering.time;

import ir.msdehghan.revrec.framework.feature.engineering.FeatureExtractor;
import ir.msdehghan.revrec.framework.model.Review;

import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

public class ActivityColumns implements FeatureExtractor {
    private static final long WEEK_IN_MILLIS = Duration.ofDays(7).toMillis();
    private static final long DAY_IN_MILLIS = Duration.ofDays(1).toMillis();
    private static final LinkedList<Long> EMPTY_LINKED_LIST = new LinkedList<>();
    public static final String LAST_REVIEW_HEADER = "LastR";
    public static final String LAST_SEEN_HEADER = "LastSeen";
    public static final String FIRST_SEEN_HEADER = "FirstSeen";
    private final LastReviewsState state;
    private final int columnCount;

    private long reviewTime;

    private Review currentReview;
    private final Map<String, LinkedList<Long>> userReviewsTimeMap = new HashMap<>(100);
    private final Map<String, LinkedList<Long>> userDevelopmentTimeMap = new HashMap<>(100);

    public ActivityColumns(LastReviewsState state) {
        this.state = state;
        columnCount = (int) (state.getDuration().toDays() / 14);
    }
    @Override
    public void setTargetReview(Review newReview) {
        var pastReview = currentReview;
        currentReview = newReview;
        reviewTime = currentReview.getSubmitTimestamp();

        // Add reviewing history
        if (pastReview != null) {
            final var pastReviewTime = pastReview.getSubmitTimestamp();
            for (String reviewer : pastReview.getReviewers()) {
                userReviewsTimeMap.computeIfAbsent(reviewer, s -> new LinkedList<>()).add(pastReviewTime);
            }

            // Add developing history
            final String developer = pastReview.getOwner();
            userDevelopmentTimeMap.computeIfAbsent(developer, s -> new LinkedList<>()).add(pastReviewTime);
        }

        // Remove outdated reviews
        for (Review review : state.getReviewsToRemove()) {
            long timestamp = review.getSubmitTimestamp();
            for (String person : review.getReviewers()) {
                userReviewsTimeMap.get(person).removeFirstOccurrence(timestamp);
            }
            userDevelopmentTimeMap.get(review.getOwner()).removeFirstOccurrence(timestamp);
        }
    }

    public List<String> getHeaders() {
        List<String> headers = new ArrayList<>(15);
        IntStream.rangeClosed(1, columnCount).mapToObj(i -> "R" + i * 2 + "w").forEach(headers::add);
        IntStream.rangeClosed(1, columnCount).mapToObj(i -> "D" + i * 2 + "w").forEach(headers::add);
        headers.addAll(List.of(LAST_REVIEW_HEADER, LAST_SEEN_HEADER, FIRST_SEEN_HEADER));
        return List.copyOf(headers);
    }

    @Override
    public void addColumnValues(Map<String, Object> row, String person) {
        LinkedList<Long> reviewTimeList = userReviewsTimeMap.getOrDefault(person, EMPTY_LINKED_LIST);
        LinkedList<Long> developTimeList = userDevelopmentTimeMap.getOrDefault(person, EMPTY_LINKED_LIST);

        addActivityColumns("R", row, reviewTimeList);
        addActivityColumns("D", row, developTimeList);

        var daysSinceLastReview = getDaysLastSeen(reviewTimeList); // DaysSinceLastReview
        row.put(LAST_REVIEW_HEADER, daysSinceLastReview == Integer.MAX_VALUE ? "" : daysSinceLastReview);

        var daysSinceLastDevelop = getDaysLastSeen(developTimeList); // DaysSinceLastDevelop
        var lastSeen = Math.min(daysSinceLastReview, daysSinceLastDevelop);
        row.put(LAST_SEEN_HEADER, lastSeen == Integer.MAX_VALUE ? "" : lastSeen); // DaysLastSeen

        var firstSeenDevelop = getDaysFirstSeen(developTimeList); // DaysSinceLastDevelop
        var firstSeenReview = getDaysFirstSeen(reviewTimeList); // DaysSinceLastDevelop
        var firstSeen = Math.max(firstSeenReview, firstSeenDevelop); // DaysSinceLastDevelop
        row.put(FIRST_SEEN_HEADER, firstSeen); // DaysLastSeen
    }

    /**
     * It is assumed that reviews are sorted by timestamp in ascending order.
     */
    private int getDaysLastSeen(LinkedList<Long> timeList) {
        if (timeList.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        final var lastSeen = timeList.getLast();
        return ((int) ((reviewTime - lastSeen) / DAY_IN_MILLIS));
    }

    private int getDaysFirstSeen(LinkedList<Long> timeList) {
        if (timeList.isEmpty()) {
            return -1;
        }
        final var firstSeen = timeList.getFirst();
        return ((int) ((reviewTime - firstSeen) / DAY_IN_MILLIS));
    }

    private void addActivityColumns(String columnPrefix, Map<String, Object> row, List<Long> timeList) {
        var count = new int[columnCount];
        for (Long timestamp : timeList) {
            final var timeDiff = reviewTime - timestamp;
            for (int i = 1; i <= columnCount; i++) {
                if (timeDiff <= i * 2 * WEEK_IN_MILLIS) {
                    count[i-1]++;
                    break;
                }
            }
        }
        for (int i = 1; i <= columnCount; i++) {
            row.put(columnPrefix + (i * 2) + "w", count[i-1]);
        }
    }
}
