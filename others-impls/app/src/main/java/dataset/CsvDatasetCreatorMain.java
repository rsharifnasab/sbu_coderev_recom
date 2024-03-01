package dataset;

import ir.msdehghan.revrec.framework.feature.engineering.*;
import ir.msdehghan.revrec.framework.feature.engineering.experience.FPScore;
import ir.msdehghan.revrec.framework.feature.engineering.time.ActivityColumns;
import ir.msdehghan.revrec.framework.feature.engineering.time.LastReviewsState;
import ir.msdehghan.revrec.framework.feature.engineering.time.TimeSinceBase;
import ir.msdehghan.revrec.framework.loader.revrec.RevRecDatasetLoader;
import ir.msdehghan.revrec.framework.model.Review;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;


public class CsvDatasetCreatorMain {
    private static final Logger logger = LogManager.getLogger(CsvDatasetCreatorMain.class);

    public static void main(String[] args) throws IOException {
        final var datasetName = "RevRec";

//        final var projectNames = List.of("react", "spark", "atom", "gerrit", "openstack", "kubernetes", "typo3");
        for (var project : RevRevProjectsLoadingTime.PROJECTS_TIME_TO_LOAD.entrySet()) {
            var projectName = project.getKey();
            var daysToKeep = project.getValue() * 30;
            logger.info("============================");
            logger.info("========== {}", projectName);
            createDataSet(datasetName, projectName, daysToKeep);
            logger.info("============================");
        }
    }

    private static void createDataSet(String datasetName, String projectName, int daysToKeep) throws IOException {
        final var datasetPath = Path.of("Datasets").resolve(datasetName);

        List<Review> reviews = new RevRecDatasetLoader(datasetPath, projectName).load();
        final long endTime = reviews.get(reviews.size() - 1).getSubmitTimestamp();
        reviews = reviews.stream()
                .filter(r -> Duration.ofMillis(endTime - r.getSubmitTimestamp()).toDays() <= daysToKeep)
                .sorted(Comparator.comparingLong(Review::getSubmitTimestamp))
                .toList();

        logger.info("Number of reviews in last {} days: {}", daysToKeep ,reviews.size());

        var state = new LastReviewsState(Duration.ofDays(56));
        List<FeatureExtractor> featureExtractors = List.of(new IdColumn(), new ActivityColumns(state),
                new FPScore(state), new TimeSinceBase(reviews.get(0).getSubmitTimestamp()),
                new IsReviewerColumn());
//        String outputPath = "Results/createdDataset/" + datasetName + "/3/" + projectName + ".csv";
        String outputPath = "Results/createdDataset/" + datasetName + "/3/time/" + projectName + ".csv";
        var datasetCreator = new DatasetCreator(Path.of(outputPath), reviews, featureExtractors, state);
        datasetCreator.run();
    }

}
