package dataset;

import ir.msdehghan.revrec.framework.metrics.CompositeMetricRegistry;
import ir.msdehghan.revrec.framework.metrics.PrecisionAtK;
import ir.msdehghan.revrec.framework.model.RecommendationResult;
import org.apache.commons.lang3.Validate;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dataset.Utils.calculateRecommendationResults;
import static dataset.Utils.readResultsFromCsv;

public class TestingOps {
    public static void main(String[] args) {
        final var javaPath = Path.of("Results/RevRec/RevFinder/56days/gerrit-recom-results.csv");
        Map<String, List<Utils.CsvResult>> allRecords = readResultsFromCsv(javaPath, "prefix", false);
        var experiment = TwoMonthSplitHardcoded.GERRIT.get(3);
        var javaRecords = experiment.getRecordsForExperiment(allRecords);
        List<RecommendationResult> javaResults = calculateRecommendationResults(javaRecords);


        final var pythonPath = Path.of("Results/LogisticRegression/RevRec/2/single_FP8w_feature/gerrit-151-211-227.csv");
        var pythonResults = calculateRecommendationResults(readResultsFromCsv(pythonPath, "1", false));

        var javaResultMap = javaResults.stream().collect(Collectors.toMap(RecommendationResult::getChangeId, Function.identity()));
        var pythonResultMap = pythonResults.stream().collect(Collectors.toMap(RecommendationResult::getChangeId, Function.identity()));

        Validate.isTrue(javaResultMap.size() == pythonResultMap.size());
        for (Map.Entry<String, RecommendationResult> entry : javaResultMap.entrySet()) {
            RecommendationResult javaValue = entry.getValue();
            RecommendationResult pythonValue = pythonResultMap.get(entry.getKey());

            try {
                Validate.isTrue(javaValue.getActual().equals(pythonValue.getActual()));
                List<String> javaRecommended = javaValue.getRecommended();
                List<String> pythonRecommended = pythonValue.getRecommended();
                var size = Math.min(javaRecommended.size(), pythonRecommended.size());
                Validate.isTrue(javaRecommended.subList(0, size).equals(pythonRecommended.subList(0, size)));
            } catch (Exception e) {
                System.out.println("pythonValue = " + pythonValue);
                System.out.println("javaValue = " + javaValue);
                System.out.println("-----------");
            }
        }
    }
}
