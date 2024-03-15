package ir.msdehghan.revrec.framework.loader.revfinder;

import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.any.Any;
import ir.msdehghan.revrec.framework.model.Review;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;

public class RevFinderReview implements Review {
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral('.')
            .appendValue(ChronoField.NANO_OF_SECOND, 9)
            .toFormatter();
    private final String changeId;
    private final String status;
    private final String project;
    private final long submitDate;
    private final long closeDate;
    private final List<String> reviewers;
    private final List<String> filePaths;

    @JsonCreator
    public RevFinderReview(
            @JsonProperty(value ="project", required = true, nullable = false) String project,
            @JsonProperty(value ="status", required = true, nullable = false) String status,
            @JsonProperty(value = "changeId", required = true, nullable = false) int changeId,
            @JsonProperty(value = "submit_date", required = true, nullable = false) String submitDate,
            @JsonProperty(value = "close_date", required = true, nullable = false) String closeDate,
            @NotNull @JsonProperty(value = "approve_history", required = true, nullable = false) List<Any> approveHistory,
            @NotNull @JsonProperty(value = "files", required = true, nullable = false) List<String> files) {
        this.status = status;
        this.changeId = String.valueOf(changeId);
        this.project = project;
        //2011-05-17 11:31:56.813000000
        this.submitDate = dateTimeFormatter.parse(submitDate, LocalDateTime::from).toInstant(ZoneOffset.UTC).toEpochMilli();
        this.closeDate = dateTimeFormatter.parse(closeDate, LocalDateTime::from).toInstant(ZoneOffset.UTC).toEpochMilli();

        if (approveHistory.isEmpty()) {
            throw new IllegalArgumentException("Review must have a reviewer");
        }

        this.reviewers = approveHistory.stream().map(n -> n.toString("userId")).toList();

        this.filePaths = files;
    }

    @Override
    public String getId() {
        return changeId;
    }

    @Override
    public String getProject() {
        return project;
    }

    @Override
    public long getSubmitTimestamp() {
        return submitDate;
    }

    @Override
    public long getClosedTimestamp() {
        return closeDate;
    }

    @Override
    public @NotNull List<String> getReviewers() {
        return reviewers;
    }

    @Override
    public @NotNull String getOwner() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<String> getFilePaths() {
        return filePaths;
    }
}
