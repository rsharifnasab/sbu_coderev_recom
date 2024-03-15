package dataset;

import ir.msdehghan.revrec.framework.loader.revrec.RevRecDatasetLoader;
import ir.msdehghan.revrec.framework.metrics.*;
import ir.msdehghan.revrec.framework.model.RecommendationResult;
import ir.msdehghan.revrec.framework.model.Review;
import ir.msdehghan.revrec.framework.output.CsvOutput;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;

public class RapidMinerFilesMetricsCalculator {
    public static void main(String[] args) throws IOException {
        final var resultsDirectory = Path.of("Results/LogisticRegression/RevRec/");
        final var projectName = "atom";
        final var resultRawFile = resultsDirectory.resolve(projectName + ".csv");
        Map<String, List<CsvResult>> result = readResultsFromCsv(resultRawFile);

        final var datasetName = "RevRec";
        final var datasetPath = Path.of("Datasets").resolve(datasetName);
        List<Review> reviews = new RevRecDatasetLoader(datasetPath, projectName).load();

        List<RecommendationResult> results = calculateRecommendationResults(reviews, result);

        CompositeMetricRegistry metrics = CompositeMetricRegistry.full(10);
        results.forEach(metrics);
        metrics.printResult();
//        CsvOutput.write(metrics, resultsDirectory.resolve(projectName + "-agg-results.csv"), Map.of(
//                "method" , "RapidMiner"
//        ));
    }

    private static List<RecommendationResult> calculateRecommendationResults(List<Review> reviewList, Map<String, List<CsvResult>> csvResults) {
        Map<String, List<String>> reviews = reviewList.stream().collect(toMap(Review::getId, Review::getReviewers));

        List<RecommendationResult> finalResults = new ArrayList<>(csvResults.size());

        for (var entry : csvResults.entrySet()) {
            String changeId = entry.getKey();
            List<String> recommendedUsers = entry.getValue().stream().map(CsvResult::userId).toList();
            List<String> actualUsers = reviews.get(changeId);
            finalResults.add(new RecommendationResult(changeId, actualUsers, recommendedUsers));
        }
        return finalResults;
    }

    private static Map<String, List<CsvResult>> readResultsFromCsv(Path resultRawFile) throws IOException {
        Map<String, List<CsvResult>> result;
        try (CSVParser parser = new CSVParser(Files.newBufferedReader(resultRawFile), CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setRecordSeparator('\n').build())) {

            result = parser.stream()
                    .map(CSVRecord::toMap)
                    .map(CsvResult::create)
                    .collect(groupingBy(CsvResult::changeId,
                            collectingAndThen(toList(),
                                    l -> l.stream().sorted(comparingDouble(CsvResult::confidence).reversed()).limit(10).toList())));
        }
        return result;
    }

    record CsvResult(String changeId, String userId, double confidence) {

        public static CsvResult create(String csvId, String conf) {
            String[] idParts = csvId.split("-", 2);
            Validate.isTrue(idParts.length == 2);
            return new CsvResult(idParts[0], idParts[1], Double.parseDouble(conf));
        }

        public static CsvResult create(Map<String, String> csvMap) {
            return CsvResult.create(csvMap.get("ID"), csvMap.get("1"));
        }
    }
}
