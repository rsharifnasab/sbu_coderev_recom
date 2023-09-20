package dataset;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class TwoMonthSplitHardcoded {
    public static final List<DataExperiment> ATOM = List.of(
            new DataExperiment(61, 121, 151, 15234, 15580),
            new DataExperiment(91, 151, 181, 15603, 15833),
            new DataExperiment(121, 181, 211, 15894, 16114),
            new DataExperiment(151, 211, 241, 16144, 16341)
    );

    public static final List<DataExperiment> GERRIT = List.of(
            new DataExperiment(61, 121, 151, 121353, 128570),
            new DataExperiment(91, 151, 181, 128491, 135670),
            new DataExperiment(121, 181, 211, 135631, 143231),
            new DataExperiment(151, 211, 227, 143232, 146710)
    );

    public static final List<DataExperiment> SPARK = List.of(
            new DataExperiment(61, 121, 151, 18947, 19231),
            new DataExperiment(91, 151, 181, 19232, 19497),
            new DataExperiment(121, 181, 211, 19499, 19736),
            new DataExperiment(151, 211, 241, 19737, 19960)
    );

    public static final List<DataExperiment> OPEN_STACK = List.of(
            new DataExperiment(61, 121, 151, 3622, 4807),
            new DataExperiment(91, 151, 181, 4808, 6052),
            new DataExperiment(121, 181, 211, 6053, 6960),
            new DataExperiment(151, 211, 241, 6961, 7956)
    );

    public record DataExperiment(int trainStart, int trainEnd, int testEnd, int testStartPrId, int testEndPrId){

        @NotNull
        public Map<String, List<Utils.CsvResult>> getRecordsForExperiment(Map<String, List<Utils.CsvResult>> allRecords) {
            return allRecords.entrySet().stream()
                    .filter(e -> {
                        var key = Integer.parseInt(e.getKey());
                        return key <= this.testEndPrId() && key >= this.testStartPrId();
                    }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
