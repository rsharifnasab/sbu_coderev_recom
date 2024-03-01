package ir.msdehghan.revrec.framework.loader.revfinder;

import com.jsoniter.JsonIterator;
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

public class RevFinderDatasetLoader implements DataLoader {
    private static final Logger logger = LogManager.getLogger(RevFinderReview.class);

    private final Path jsonFilePath;
    private final String projectName;

    public RevFinderDatasetLoader(@NotNull Path basePath, @NotNull String projectName) {
        this.projectName = projectName;
        jsonFilePath = basePath.resolve(projectName + ".json");
        if (!Files.exists(jsonFilePath) || !Files.isReadable(jsonFilePath)) {
            throw new IllegalArgumentException(jsonFilePath + " does not exists.");
        }
    }

    @Override
    public List<Review> load() throws IOException {
        var revFinderReviews = Files.lines(jsonFilePath)
                .map(line -> JsonIterator.deserialize(line, RevFinderReview.class)).toList();
        logger.info("{} reviews loaded from '{}.json'", revFinderReviews.size(), projectName);
        List<Review> filteredReviews = revFinderReviews.stream()
                .map(Review.class::cast)
                .filter(r -> !r.getFilePaths().isEmpty())
                .sorted(Comparator.comparingLong(Review::getSubmitTimestamp)).toList();
        logger.info("{} reviews are filtered because of having no file paths",
                revFinderReviews.size() - filteredReviews.size());
        return filteredReviews;
    }
}
