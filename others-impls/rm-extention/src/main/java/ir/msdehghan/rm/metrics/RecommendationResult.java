package ir.msdehghan.rm.metrics;

import java.util.List;
import java.util.Objects;

public class RecommendationResult {
    private final String changeId;
    private final List<String> recommended;
    private final List<String> actual;

    public RecommendationResult(String changeId, List<String> actual, List<String> recommended) {
        this.changeId = Objects.requireNonNull(changeId);
        this.recommended = Objects.requireNonNull(recommended);
        this.actual = Objects.requireNonNull(actual);
    }

    public String getChangeId() {
        return changeId;
    }

    public List<String> getRecommended() {
        return recommended;
    }

    public List<String> getActual() {
        return actual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationResult that = (RecommendationResult) o;
        return Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeId);
    }

    @Override
    public String toString() {
        return "RecommendationResult{" + '\n' +
                "recommended=" + recommended + '\n' +
                "actual=" + actual +
                '}';
    }
}
