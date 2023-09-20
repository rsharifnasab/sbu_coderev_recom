package dataset;

import ir.msdehghan.revrec.framework.model.RecommendationResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;

public class Utils {
    public static Map<String, List<CsvResult>> readResultsFromCsv(Path resultRawFile, String targetColumn,
                                                                  boolean isRank) {
        Map<String, List<CsvResult>> result;
        try (CSVParser parser = new CSVParser(Files.newBufferedReader(resultRawFile), CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setRecordSeparator('\n').build())) {

            result = parser.stream()
                    .map(CSVRecord::toMap)
                    .map(columns -> CsvResult.createFromMap(columns, targetColumn, isRank))
                    .collect(groupingBy(CsvResult::changeId, toList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static List<RecommendationResult> calculateRecommendationResults(Map<String, List<CsvResult>> csvResults) {
        List<RecommendationResult> finalResults = new ArrayList<>(csvResults.size());

        for (var entry : csvResults.entrySet()) {
            String changeId = entry.getKey();
            List<CsvResult> results = entry.getValue();
            List<String> recommendedUsers = results.stream()
                    .filter(CsvResult::recommended)
                    .sorted(comparingDouble(CsvResult::confidence).reversed())
                    .limit(10)
                    .map(CsvResult::userId)
                    .toList();
            List<String> actualUsers = results.stream().filter(CsvResult::isReviewer).map(CsvResult::userId).toList();
            finalResults.add(new RecommendationResult(changeId, actualUsers, recommendedUsers));
        }
        return finalResults;
    }

    public record CsvResult(String changeId, String userId, double confidence, boolean isReviewer, boolean recommended) {

        public static CsvResult createFromMap(Map<String, String> csvMap, String columnName, boolean isRank) {
            String valueString = csvMap.getOrDefault(columnName, "").trim();
            double confidence;
            boolean recommended = true;
            if (valueString.isBlank()){
                recommended = false;
                confidence = -1;
            } else {
                var value = parseDouble(valueString);
                confidence = isRank ? 101 - value : value;
            }
            return new CsvResult(csvMap.get("pr_id"), csvMap.get("person_id"), confidence,
                    csvMap.get("is_reviewer").equals("1"), recommended);
        }
    }
}
