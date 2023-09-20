package ir.msdehghan.revrec.framework.feature.engineering;

import ir.msdehghan.revrec.framework.feature.engineering.time.LastReviewsState;
import ir.msdehghan.revrec.framework.model.Review;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class DatasetCreator {
    private static final Logger logger = LogManager.getLogger(DatasetCreator.class);
    private final Path datasetFile;
    private final String[] headers;
    private final Map<String, MutableInt> persons = new HashMap<>(100);
    private final List<Review> reviewList;
    private final List<FeatureExtractor> featureExtractors;
    private final LastReviewsState state;

    public DatasetCreator(Path resultCsvFile, List<Review> reviews, List<FeatureExtractor> featureExtractors,
                          LastReviewsState state) {
        this.datasetFile = resultCsvFile;
        this.reviewList = reviews;
        this.state = state;
        this.featureExtractors = featureExtractors;
        this.headers = featureExtractors.stream().map(FeatureExtractor::getHeaders)
                .flatMap(Collection::stream).toArray(String[]::new);
    }

    /**
     * It is assumed that reviews are sorted by timestamp in ascending order.
     */
    public void run() throws IOException {
        Files.createDirectories(datasetFile.getParent());
//        final var fileWriter = Files.newBufferedWriter(datasetFile, UTF_8, TRUNCATE_EXISTING, CREATE);
        final var fileWriter = NullWriter.nullWriter();
        final var timeWriter = Files.newBufferedWriter(datasetFile, UTF_8, TRUNCATE_EXISTING, CREATE);
        runInternal(fileWriter, timeWriter);
        timeWriter.close();
    }

    private void runInternal(Writer fileWriter, Writer timeWriter) throws IOException {
        try(fileWriter) {
            fileWriter.write(String.join(",", headers));
            fileWriter.append('\n');

            int counter = 0;
            for (Review review : reviewList) {
                long startTime = System.currentTimeMillis();
                handleNewReview(review);
                for (String person : persons.keySet()) {
                    final var rowMap = handlePerson(person);
                    String csvLine = createCsvLine(rowMap);
                    if (csvLine.length() > 5) { counter++; }
                    fileWriter.write(csvLine);
                }
                long elapsed = System.currentTimeMillis() - startTime;
                if (timeWriter != null) {
                    timeWriter.write(review.getId() + "," + elapsed + "\n");
                }
//                counter++;
                if (counter % 1000 == 1) {
                    logger.info("Processed {} reviews. Found {} persons", counter, persons.size());
                }
            }
        }
    }

    private void handleNewReview(Review review) {
        state.addToState(review);
        for (FeatureExtractor featureExtractor : featureExtractors) {
            featureExtractor.setTargetReview(review);
        }

        for (String reviewer : review.getReviewers()) {
            persons.computeIfAbsent(reviewer, s -> new MutableInt(0)).increment();
        }

        for (Review toRemove : state.getReviewsToRemove()) {
            for (String reviewer : toRemove.getReviewers()) {
                persons.compute(reviewer, (s, v) -> {
                    if (v.intValue() == 1){
                        return  null;
                    } else {
                        v.decrement();
                        return v;
                    }
                });
            }
        }
    }

    private Map<String, Object> handlePerson(String person) {
        Map<String, Object> row = new HashMap<>(headers.length);
        featureExtractors.forEach(featureExtractor -> featureExtractor.addColumnValues(row, person));
        return row;
    }

    private String createCsvLine(Map<String, Object> row) {
        StringBuilder csvLine = new StringBuilder(100);
        csvLine.append(row.getOrDefault(headers[0], "").toString());
        for (int i = 1; i < headers.length; i++) {
            csvLine.append(',').append(row.getOrDefault(headers[i], "").toString());
        }
        return csvLine.append('\n').toString();
    }
}
