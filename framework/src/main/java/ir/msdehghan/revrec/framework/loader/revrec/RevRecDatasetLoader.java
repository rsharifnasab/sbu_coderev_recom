package ir.msdehghan.revrec.framework.loader.revrec;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import ir.msdehghan.revrec.framework.loader.DataLoader;
import ir.msdehghan.revrec.framework.model.Review;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class RevRecDatasetLoader implements DataLoader {
    private static final Logger logger = LogManager.getLogger(RevRecDatasetLoader.class);

    private final Path jsonFilePath;
    private final String projectName;

    public RevRecDatasetLoader(@NotNull Path basePath, @NotNull String projectName) {
        this.projectName = projectName;
        jsonFilePath = basePath.resolve(projectName + ".json");
        if (!Files.exists(jsonFilePath) || !Files.isReadable(jsonFilePath)) {
            throw new IllegalArgumentException(jsonFilePath + " does not exists.");
        }
    }

    @Override
    public List<Review> load() throws IOException {
        String fileContent = Files.readString(jsonFilePath);
        List<Any> reviewList = JsonIterator.deserialize(fileContent).asList();
//        logger.info("{} reviews loaded from '{}.json'", reviewList.size(), projectName);
        List<Review> filteredReviews = reviewList.stream().map(any -> any.as(RevRecReview.class))
                .map(Review.class::cast)
                .filter(r -> !r.getFilePaths().isEmpty() && !r.getReviewers().isEmpty())
                .sorted(Comparator.comparingLong(Review::getSubmitTimestamp))
                .toList();
        if (projectName.equals("android")) {
            filteredReviews = filteredReviews.stream()
                    .filter(r -> r.getSubmitTimestamp() < 1326339200000L)
                    .toList();
        }
//        logger.info("{} reviews are filtered because of having no file paths",
//                reviewList.size() - filteredReviews.size());
        return filteredReviews;
    }
}
