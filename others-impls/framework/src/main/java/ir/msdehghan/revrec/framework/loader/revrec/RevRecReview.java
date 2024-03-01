package ir.msdehghan.revrec.framework.loader.revrec;

import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.any.Any;
import ir.msdehghan.revrec.framework.model.Review;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RevRecReview implements Review {

    private final String changeNumber;
    private final String subProject;
    private final long timestamp;

    @NotNull
    private final List<String> reviewers;

    @NotNull
    private final String owner;

    @NotNull
    private final List<String> filePaths;

    @JsonCreator
    public RevRecReview(
            @JsonProperty("subProject") String subProject,
            @JsonProperty(value = "changeNumber", required = true, nullable = false) int changeNumber,
            @JsonProperty(value = "timestamp", required = true, nullable = false) long timestamp,
            @NotNull @JsonProperty(value = "reviewers", required = true, nullable = false) List<Any> reviewers,
            @NotNull @JsonProperty(value = "owner", required = true, nullable = false) Any owner,
            @NotNull @JsonProperty(value = "filePaths", required = true, nullable = false) List<Any> filePaths) {
        this.changeNumber = String.valueOf(changeNumber);
        this.subProject = subProject;
        this.timestamp = timestamp;
        this.owner = owner.toString("accountId");
        this.reviewers = reviewers.stream()
                .map(n -> n.toString("accountId")).toList();

        this.filePaths = filePaths.stream()
                .map(a -> a.toString("location")).toList();
    }

    @Override
    public String getId() {
        return changeNumber;
    }

    @Override
    public String getProject() {
        return subProject;
    }

    @Override
    public long getSubmitTimestamp() {
        return timestamp;
    }

    @Override
    public @NotNull List<String> getReviewers() {
        return reviewers;
    }

    @Override
    public @NotNull String getOwner() {
        return owner;
    }

    @Override
    public @NotNull List<String> getFilePaths() {
        return filePaths;
    }
}
