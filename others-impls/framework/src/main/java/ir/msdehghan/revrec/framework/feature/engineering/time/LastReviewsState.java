package ir.msdehghan.revrec.framework.feature.engineering.time;

import ir.msdehghan.revrec.framework.model.Review;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LastReviewsState {
    private final long TIME;
    private final Duration duration;
    private final LinkedList<Review> reviews = new LinkedList<>();
    private final LinkedList<Review> reviewsToRemove = new LinkedList<>();
    private final List<Review> reviewsToRemoveUnmodified = Collections.unmodifiableList(reviewsToRemove);
    private final List<Review> reviewsUnmodified = Collections.unmodifiableList(reviews);

    private long minTimestampToKeep = Long.MIN_VALUE;

    private Review currentReview = null;

    public LastReviewsState(Duration duration) {
        TIME = duration.toMillis();
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    public long getMinTimestampToKeep() {
        return minTimestampToKeep;
    }

    public void addToState(Review review) {
        var prevReview = currentReview;
        currentReview = review;

        if (prevReview != null) {
            reviews.addLast(prevReview);
        }
        reviewsToRemove.clear();
        minTimestampToKeep = review.getSubmitTimestamp() - TIME;
        while (!reviews.isEmpty() && reviews.getFirst().getSubmitTimestamp() < minTimestampToKeep) {
            reviewsToRemove.addLast(reviews.removeFirst());
        }
    }

    public List<Review> getReviewsToRemove() {
        return reviewsToRemoveUnmodified;
    }

    public List<Review> getReviews() {
        return reviewsUnmodified;
    }
}
