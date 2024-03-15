import dataset.RevRevProjectsLoadingTime;
import ir.msdehghan.revrec.framework.evaluator.MethodEvaluator;
import ir.msdehghan.revrec.framework.loader.revrec.RevRecDatasetLoader;
import ir.msdehghan.revrec.framework.metrics.CompositeMetricRegistry;
import ir.msdehghan.revrec.framework.model.RecommendationResult;
import ir.msdehghan.revrec.framework.model.Review;
import ir.msdehghan.revrec.framework.output.CsvOutput;
import ir.msdehghan.revrec.framework.recommendation.MostActive;
import ir.msdehghan.revrec.framework.recommendation.RecommendationMethod;
import ir.msdehghan.revrec.framework.recommendation.revfinder.RevFinder;
import ir.msdehghan.revrec.framework.strategy.MovingStrategy;
import ir.msdehghan.revrec.framework.strategy.TemporalMovingStrategy;
import ir.msdehghan.revrec.framework.strategy.ValidationStrategy;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Main {

    private static final int K = 10;

    public static void main(String[] args) throws IOException {
        final var datasetName = "RevRec";
        var projects = List.of("atom", "react", "gerrit", "spark", "openstack", "typo3", "kubernetes");
//        var projects = List.of("atom", "react", "gerrit", "spark", "openstack");

        for (var project : RevRevProjectsLoadingTime.PROJECTS_TIME_TO_LOAD.entrySet()) {
            String projectName = project.getKey();
//            int daysToKeep = project.getValue() * 30;
            int daysToKeep = 6 * 30;

//            var filePath = Path.of("Results/RevRec/RevFinder/56days").resolve(projectName + "-recom-results.csv");
            var filePath = Path.of("Results/RevRec/RevFinder/56days/time").resolve(projectName + ".csv");
            List<Review> reviews = loadDataset(datasetName, projectName);
            reviews = filterReviewsForLastDays(reviews, daysToKeep);
            var strategy = TemporalMovingStrategy.create(reviews, Duration.ofDays(56), Review::getSubmitTimestamp);
            var method = new RevFinder(K);
//            var method = new MostActive(K);

//            evaluateAndWriteRecommendedToCsv(filePath, strategy, method);
            evaluateAndWriteTimes(filePath, strategy, method);
            System.out.println("----------------------------");
        }
    }

    private static void evaluateAndWriteTimes(Path filePath, ValidationStrategy<Review> strategy,
                                                         RecommendationMethod method) throws IOException {
        final var headers = List.of("ID", "time");
        try (CsvOutput csvOutput = new CsvOutput(headers, filePath)) {
            var evaluator = new MethodEvaluator(strategy, method, result -> {
                final var prId = result.getChangeId();
                try {
                    csvOutput.write(List.of(prId, String.valueOf(result.getTime())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            evaluator.run();
        }
    }

    private static void evaluateAndWriteRecommendedToCsv(Path filePath, ValidationStrategy<Review> strategy,
                                                         RecommendationMethod method) throws IOException {
        final var originalHeaders = List.of("pr_id", "person_id", "recom_rank", "is_reviewer");
        final var metadataNames = method.getMetadataNames();
        final var headers = Stream.of(originalHeaders, metadataNames).flatMap(List::stream).toList();
        try (CsvOutput csvOutput = new CsvOutput(headers, filePath)) {
            var evaluator = new MethodEvaluator(strategy, method, result -> {
                final var prId = result.getChangeId();
                final List<String> recommended = result.getRecommended();
                Set<String> actual = new HashSet<>(result.getActual());
                try {
                    for (int i = 0; i < recommended.size(); i++) {
                        String person = recommended.get(i);
                        boolean isReviewer = actual.remove(person);
                        csvOutput.write(createRow(
                                List.of(prId, person, String.valueOf(i + 1), isReviewer ? "1" : "0"),
                                metadataNames, person, result
                        ));
                    }
                    for (String person : actual) {
                        csvOutput.write(createRow(
                                List.of(prId, person, "", "1"), metadataNames, person, result
                        ));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            evaluator.run();
        }
    }

    public static List<String> createRow(List<String> originalHeaders, List<String> metadataNames, String person,
                                         RecommendationResult result) {
        return Stream.concat(originalHeaders.stream(),
                metadataNames.stream().map(name -> {
                    Number score = result.getMetadataForPerson(name, person);
                    return score == null ? "" : "%.3f".formatted(score.doubleValue());
                })
        ).toList();
    }

    private static void mainRunAndSaveMetricsToCsv() throws IOException {
        final var datasetName = "RevRec";
        final var projectName = "atom";

        List<Review> reviews = loadDataset(datasetName, projectName);
        reviews = filterReviewsForLastDays(reviews, 8 * 30);

        var strategy = TemporalMovingStrategy.create(reviews, Duration.ofDays(60), Review::getSubmitTimestamp);
        var method = new MostActive(K);
        CompositeMetricRegistry metrics = evaluateMethod(strategy, method);

        metrics.printResult();

        writeResultMetricsToFile(projectName, method, metrics);
    }

    private static void writeResultMetricsToFile(String projectName, RecommendationMethod method,
                                                 CompositeMetricRegistry metrics) throws IOException {
        Path csvResultPath = Path.of("Results/createdDataset/RevRec/result").resolve(projectName + "-agg-results.csv");
        CsvOutput.write(metrics, csvResultPath, Map.of(
                "method", method.getClass().getSimpleName()
                // Maybe we need to add train_start and others?
        ));
    }

    @NotNull
    private static CompositeMetricRegistry evaluateMethod(MovingStrategy<Review> strategy, RecommendationMethod method) {
        CompositeMetricRegistry metrics = CompositeMetricRegistry.full(10);
        var evaluator = new MethodEvaluator(strategy, method, metrics);
        evaluator.run();
        return metrics;
    }

    @NotNull
    private static List<Review> filterReviewsForLastDays(List<Review> reviews, int days) {
        final long endTime = reviews.get(reviews.size() - 1).getSubmitTimestamp();
        return reviews.stream()
                .filter(r -> Duration.ofMillis(endTime - r.getSubmitTimestamp()).toDays() <= days)
                .toList();
    }

    private static List<Review> loadDataset(String datasetName, String projectName) throws IOException {
        final var datasetPath = Path.of("Datasets").resolve(datasetName);
        return new RevRecDatasetLoader(datasetPath, projectName).load();
    }
}
