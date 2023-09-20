package dataset;

import ir.msdehghan.revrec.framework.metrics.CompositeMetricRegistry;
import ir.msdehghan.revrec.framework.model.RecommendationResult;
import ir.msdehghan.revrec.framework.output.CsvOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class PythonFilesMetricsCalculator {
    public static void main(String[] args) {
        final var dataDir = Path.of("Results/RevRec/MostActive/56days/");
        final var isRank = true;
//        var colNames = List.of("prefix","suffix","subsequence","substring","finalScore");
        var colNames = List.of("recom_rank");
        for (final String targetColumn : colNames) {
//            final var method = "RevFinder_" + targetColumn;
            final var method = "MostActive";
            var projects = Map.ofEntries(
                    Map.entry("typo3", CreatedDataset3.TYPO3),
                    Map.entry("spark", CreatedDataset3.SPARK),
                    Map.entry("tensorflow", CreatedDataset3.TENSORFLOW),
                    Map.entry("moby", CreatedDataset3.MOBY),
                    Map.entry("angular", CreatedDataset3.ANGULAR),
                    Map.entry("swift", CreatedDataset3.SWIFT),
                    Map.entry("bitcoin", CreatedDataset3.BITCOIN),
                    Map.entry("opencv", CreatedDataset3.OPENCV),
                    Map.entry("django", CreatedDataset3.DJANGO),
                    Map.entry("openstack", CreatedDataset3.OPENSTACK),
                    Map.entry("homebrew-core", CreatedDataset3.HOMEBREW_CORE),
                    Map.entry("react", CreatedDataset3.REACT),
                    Map.entry("react-native", CreatedDataset3.REACT_NATIVE),
                    Map.entry("libreoffice", CreatedDataset3.LIBREOFFICE),
                    Map.entry("go", CreatedDataset3.GO),
                    Map.entry("threejs", CreatedDataset3.THREEJS),
                    Map.entry("gerrit", CreatedDataset3.GERRIT)
            );
            for (Map.Entry<String, List<TwoMonthSplitHardcoded.DataExperiment>> entry : projects.entrySet()) {
                String projectName = entry.getKey();
                var path = dataDir.resolve(projectName + "-recom-results.csv");
                Map<String, List<Utils.CsvResult>> allRecords = Utils.readResultsFromCsv(path, targetColumn, isRank);
                calcAndWriteResultForProject(dataDir, projectName, method, allRecords, entry.getValue());
            }
        }
    }

    private static void calcAndWriteResultForProject(Path resultsDirectory, String projectName, String method,
                                                     Map<String, List<Utils.CsvResult>> allRecords,
                                                     List<TwoMonthSplitHardcoded.DataExperiment> split) {
        for (var experiment : split) {
            Map<String, List<Utils.CsvResult>> filtered = experiment.getRecordsForExperiment(allRecords);
            var results = Utils.calculateRecommendationResults(filtered);
            CompositeMetricRegistry metrics = CompositeMetricRegistry.full(10);
            results.forEach(metrics);
            try {
                CsvOutput.write(metrics, resultsDirectory.resolve("agg-results.csv"), List.of(
                        Map.entry("test_end" , String.valueOf(experiment.testEnd())),
                        Map.entry("train_end" , String.valueOf(experiment.trainEnd())),
                        Map.entry("train_start" , String.valueOf(experiment.trainStart())),
                        Map.entry("method" , method),
                        Map.entry("project" , projectName)
                ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void mainOld3(String[] args) {
        final var path = Path.of("Results/LogisticRegression/RevRec/2/single_FP8w_feature/gerrit-151-211-227.csv");
        var results = Utils.calculateRecommendationResults(Utils.readResultsFromCsv(path, "1", false));
        CompositeMetricRegistry metrics = CompositeMetricRegistry.full(10);
        results.forEach(metrics);
        metrics.printResult();
    }

    public static void mainOld(String[] args) {
        final var path = Path.of("Results/RevRec/RevFinder/56days/gerrit-recom-results.csv");
        Map<String, List<Utils.CsvResult>> allRecords = Utils.readResultsFromCsv(path, "finalScore", false);
        calcAndWriteResultForProject(path, "projectName", "method", allRecords,
                List.of(TwoMonthSplitHardcoded.GERRIT.get(3)));
    }

    public static void mainOld2(String[] args) throws IOException {
        final var resultsDirectory = Path.of("Results/LogisticRegression/RevRec/2/enriched/atom-61-121-151.csv");
        Files.list(resultsDirectory).forEach(path -> {
            var fileName = path.getFileName().toString();
            String[] p = fileName.substring(0, fileName.lastIndexOf('.')).split("-", 4);
            var results = Utils.calculateRecommendationResults(Utils.readResultsFromCsv(path, "1", false));
            CompositeMetricRegistry metrics = CompositeMetricRegistry.full(10);
            results.forEach(metrics);
            try {
                CsvOutput.write(metrics, resultsDirectory.resolve("agg-results.csv"), List.of(
                        Map.entry("test_end" , p[3]),
                        Map.entry("train_end" , p[2]),
                        Map.entry("train_start" , p[1]),
                        Map.entry("method" , "LogisticRegression_new_pipeline"),
                        Map.entry("project" , p[0])
                ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
