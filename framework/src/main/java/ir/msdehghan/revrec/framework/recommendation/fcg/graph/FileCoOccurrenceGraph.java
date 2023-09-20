package ir.msdehghan.revrec.framework.recommendation.fcg.graph;

import ir.msdehghan.revrec.framework.model.Review;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileCoOccurrenceGraph {
    private static final Logger logger = LogManager.getLogger(FileCoOccurrenceGraph.class);

    private final HashMap<FileCoOccurrence, Integer> fileCoOccurrenceCounter = new HashMap<>(23000);
    private final Graph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    private final Map<String, SingleSourcePaths<String, DefaultWeightedEdge>> shortestPaths = new HashMap<>();

    public double getDistance(String source, String target) {
        return shortestPaths
                .computeIfAbsent(source, s -> new DijkstraShortestPath<>(graph).getPaths(source))
                .getWeight(target);
    }

    public boolean containsVertex(String file) {
        return graph.containsVertex(file);
    }

    public void addReviews(@NotNull List<Review> reviews) {
        reviews.forEach(this::addReview);
        shortestPaths.clear();
    }

    private void addReview(Review review) {
        List<String> filePaths = review.getFilePaths();
        final var size = filePaths.size();
        for (int i = 0; i < size; i++) {
            final var source = filePaths.get(i);
            for (int j = i + 1; j < size; j++) {
                FileCoOccurrence fileCoOccurrence = new FileCoOccurrence(source, filePaths.get(j));
                if (fileCoOccurrence.source.equals(fileCoOccurrence.target)) {
                    logger.warn("Duplicate file: Id:{}, file:{}", review.getId(), fileCoOccurrence.source);
                    continue;
                }
                Integer newValue = fileCoOccurrenceCounter.compute(fileCoOccurrence, (k, v) -> v == null ? 1 : v + 1);
                addEdge(fileCoOccurrence, newValue);
            }
        }
    }

    private void addEdge(FileCoOccurrence coOccurrence, int value) {
        graph.addVertex(coOccurrence.source);
        graph.addVertex(coOccurrence.target);
        graph.addEdge(coOccurrence.source, coOccurrence.target);
        graph.setEdgeWeight(coOccurrence.source, coOccurrence.target, 1.0 / value);
    }

    public void deleteReviews(@NotNull List<Review> reviews) {
        reviews.forEach(this::deleteReview);
        shortestPaths.clear();
    }

    private void deleteReview(Review review) {
        List<String> filePaths = review.getFilePaths();
        final var size = filePaths.size();
        for (int i = 0; i < size; i++) {
            final var source = filePaths.get(i);
            for (int j = i + 1; j < size; j++) {
                FileCoOccurrence fileCoOccurrence = new FileCoOccurrence(source, filePaths.get(j));
                Integer newValue = fileCoOccurrenceCounter.computeIfPresent(fileCoOccurrence, (k, v) -> v == 1 ? null : v - 1);
                deleteEdge(fileCoOccurrence, newValue);
            }
        }
    }

    private void deleteEdge(FileCoOccurrence coOccurrence, Integer value) {
        if (value == null) {
            graph.removeEdge(coOccurrence.source, coOccurrence.target);
            if (graph.degreeOf(coOccurrence.source) == 0) {
                graph.removeVertex(coOccurrence.source);
            }
            if (graph.degreeOf(coOccurrence.target) == 0) {
                graph.removeVertex(coOccurrence.target);
            }
        } else {
            graph.setEdgeWeight(coOccurrence.source, coOccurrence.target, 1.0 / value);
        }
    }
}
