package tests;

import ir.msdehghan.revrec.framework.loader.revrec.RevRecDatasetLoader;
import ir.msdehghan.revrec.framework.model.Review;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class TempDatasetOps {
    private static final int K = 10;
    public static void main(String[] args) throws IOException {
        final var datasetName = "RevRec";
        final var projectName = "kubernetes";

        final var datasetPath = Path.of("Datasets").resolve(datasetName);

        List<Review> reviews = new RevRecDatasetLoader(datasetPath, projectName).load();
        printTimeDiff(reviews);

    }

    private static void printTimeDiff(List<Review> reviews) {
        var last = reviews.get(reviews.size() - 1).getSubmitTimestamp();
        var first = reviews.get(0).getSubmitTimestamp();
        System.out.println(Duration.ofMillis(last - first).toDays());
    }

    private static void printOwnerAsReviewer(List<Review> reviews) {
        reviews = reviews.stream()
                .filter(r -> r.getReviewers().contains(r.getOwner()))
                .filter(r -> r.getReviewers().size() == 1)
                .collect(Collectors.toList());

        Review review = reviews.get(reviews.size() - 100);
        System.out.println("review.getId() = " + review.getId());
        System.out.println("review.getProject() = " + review.getProject());
    }
}
