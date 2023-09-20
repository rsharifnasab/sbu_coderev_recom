package ir.msdehghan.rm;

import org.apache.commons.lang3.Validate;

import java.util.Objects;

class Result {
    String changeId;
    String userId;
    double confidence;
    boolean isReviewer;

    public Result(String changeId, String userId, double confidence, boolean isReviewer) {
        this.changeId = changeId;
        this.userId = userId;
        this.confidence = confidence;
        this.isReviewer = isReviewer;
    }

    static Result create(String csvId, double conf, String isReviewer) {
        String[] idParts = csvId.split("-", 2);
        Validate.isTrue(idParts.length == 2);
        return new Result(idParts[0], idParts[1], conf, isReviewer.equals("1"));
    }

    public String getChangeId() {
        return changeId;
    }

    public String getUserId() {
        return userId;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean isReviewer() {
        return isReviewer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return changeId.equals(result.changeId) && userId.equals(result.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeId, userId);
    }
}
