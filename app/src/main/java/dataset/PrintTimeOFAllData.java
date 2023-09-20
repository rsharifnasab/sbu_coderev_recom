package dataset;

import ir.msdehghan.revrec.framework.loader.revrec.RevRecDatasetLoader;
import ir.msdehghan.revrec.framework.model.Review;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PrintTimeOFAllData {

    public static void main(String[] args) throws IOException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        final Path datasetBasePath = Path.of("Datasets/RevRec");
        System.out.println("projectName,start,end,developers,reviewers,reviews");
        for (var project : RevRevProjectsLoadingTime.PROJECTS_TIME_TO_LOAD.entrySet()) {
            String projectName = project.getKey();
            int daysToKeep = project.getValue() * 30;
            List<Review> reviews = new RevRecDatasetLoader(datasetBasePath, projectName).load();
            final long endTime = reviews.get(reviews.size() - 1).getSubmitTimestamp();
            reviews = reviews.stream()
                    .filter(r -> Duration.ofMillis(endTime - r.getSubmitTimestamp()).toDays() <= daysToKeep)
                    .toList();
            var line = projectName + ',';
            line += formatter.format(Instant.ofEpochMilli(reviews.get(0).getSubmitTimestamp()).atZone(ZoneId.systemDefault())) + ',';
            line += formatter.format(Instant.ofEpochMilli(reviews.get(reviews.size() - 1).getSubmitTimestamp()).atZone(ZoneId.systemDefault())) + ',';
            line += reviews.stream().map(Review::getOwner).distinct().count() + ",";
            line += reviews.stream().flatMap(review -> review.getReviewers().stream()).distinct().count() + ",";
            line += reviews.size();
            System.out.println(line);
        }
    }


    public static void main2(String[] args) throws IOException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        final Path datasetBasePath = Path.of("Datasets/RevRec");
        List<String> projectNames = Files.list(datasetBasePath)
                .sorted(Comparator.<Path>comparingLong(p -> p.toFile().length()).reversed())
                .map(Path::toString)
                .map(FilenameUtils::getBaseName)
                .toList();

//        LocalDate firstTime = LocalDate.of(2008,10,1);
        LocalDate firstTime = LocalDate.of(2016, 6, 1);
        LocalDate lastTime = LocalDate.of(2017, 12, 1);

        System.out.printf("%-20s", "Year");
        for (var i = firstTime; !i.isAfter(lastTime); i = i.plusMonths(1)) {
            System.out.printf("%5s", i.getYear());
        }
        System.out.println();

        System.out.printf("%-20s", "Month");
        for (var i = firstTime; !i.isAfter(lastTime); i = i.plusMonths(1)) {
            System.out.printf("%5s", i.getMonthValue());
        }
        System.out.println();

        for (String projectName : projectNames) {
            RevRecDatasetLoader loader = new RevRecDatasetLoader(datasetBasePath, projectName);
            Map<String, MutableInt> projectDataMap = new HashMap<>();
            var monthCount = 0;
            var totalCount = 0L;

            List<Review> dataset = loader.load();
            for (Review review : dataset) {
                var time = LocalDate.ofEpochDay(TimeUnit.MILLISECONDS.toDays(review.getSubmitTimestamp()));
                projectDataMap.computeIfAbsent(formatter.format(time), s -> new MutableInt()).increment();
            }
            System.out.printf("%-20s", projectName);
            for (var i = firstTime; !i.isAfter(lastTime); i = i.plusMonths(1)) {
                var count = projectDataMap.getOrDefault(formatter.format(i), new MutableInt()).longValue();
                totalCount += count;
                monthCount++;
                System.out.printf("%5s", count);
            }
            System.out.printf("   = %5d/%4d", totalCount, totalCount / monthCount);
            System.out.println();
        }
    }
}
