package ir.msdehghan.revrec.framework.model;

import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RecommendationResult {
    private final String changeId;
    private final List<String> recommended;
    private final List<String> actual;

    private final Map<String, ? extends Map<String, ? extends Number>> metadata;

    private final long time;

    @JsonCreator
    public RecommendationResult(
            @JsonProperty(value = "changeId", required = true, nullable = false) String changeId,
            @JsonProperty(value = "actual", required = true, nullable = false) List<String> actual,
            @JsonProperty(value = "recommended", required = true, nullable = false) List<String> recommended) {
        this(changeId, actual, recommended, null);
    }

    public RecommendationResult(String changeId, List<String> actual, List<String> recommended,
                                Map<String, ? extends Map<String, ? extends Number>> metadata) {
        this.changeId = Objects.requireNonNull(changeId);
        this.recommended = Objects.requireNonNull(recommended);
        this.actual = Objects.requireNonNull(actual);
        this.metadata = metadata;
        this.time = 0;
    }

    public RecommendationResult(String changeId, List<String> actual, List<String> recommended, long time) {
        this.changeId = Objects.requireNonNull(changeId);
        this.recommended = Objects.requireNonNull(recommended);
        this.actual = Objects.requireNonNull(actual);
        this.time = time;
        this.metadata = null;
    }

    @VisibleForTesting
    public RecommendationResult(List<Integer> actual, List<Integer> recommended) {
        this.changeId = "0";
        this.recommended = recommended.stream().map(String::valueOf).collect(Collectors.toList());
        this.actual = actual.stream().map(String::valueOf).collect(Collectors.toList());
        this.metadata = null;
        this.time = 0;
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

    public Number getMetadataForPerson(String metadataName, String personName) {
        Objects.requireNonNull(metadata, "metadata is not set");
        var metadataMap = metadata.get(metadataName);
        Objects.requireNonNull(metadataMap, () -> metadataName + "does not exists");
        return metadataMap.get(personName);
    }

    public long getTime() {
        return time;
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
                "changeId=" + changeId + '\n' +
                "recommended=" + recommended + '\n' +
                "actual=" + actual +
                '}';
    }
}
