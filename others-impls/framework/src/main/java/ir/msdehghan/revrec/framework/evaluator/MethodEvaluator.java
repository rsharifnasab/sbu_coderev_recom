package ir.msdehghan.revrec.framework.evaluator;

import ir.msdehghan.revrec.framework.model.RecommendationResult;
import ir.msdehghan.revrec.framework.model.Review;
import ir.msdehghan.revrec.framework.recommendation.RecommendationMethod;
import ir.msdehghan.revrec.framework.recommendation.revfinder.FilePathSimilarity;
import ir.msdehghan.revrec.framework.strategy.ValidationStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.function.Consumer;

public class MethodEvaluator implements Runnable {
    private static final Logger logger = LogManager.getLogger(MethodEvaluator.class);

    private final ValidationStrategy<Review> strategy;
    private final RecommendationMethod method;
    private final Consumer<RecommendationResult> resultConsumer;
    private long startTime;
    private long lastLogTime;
    private long counter = 0;

    public MethodEvaluator(ValidationStrategy<Review> strategy,
                           RecommendationMethod method,
                           Consumer<RecommendationResult> resultConsumer) {
        this.strategy = strategy;
        this.method = method;
        this.resultConsumer = resultConsumer;
    }

    @Override
    public void run() {
        logger.info("Starting evaluation of {}", method.getClass().getSimpleName());
        startTime = lastLogTime = System.currentTimeMillis();

        method.addToState(strategy.getTrainData());
        while (strategy.hasTestData()) {
            method.setPastReviews(strategy.getTrainData());

            strategy.getTestData().stream().map(method::recommendReviewers).forEach(resultConsumer);
            strategy.testDataProcessed();

            method.removeFromState(strategy.getRowsToRemoveFromTrain());
            method.addToState(strategy.getRowsToAddToTrain());

            counter++;
            logIfNeeded();
        }
        logData();
    }

    private void logIfNeeded() {
        final long elapsedTimeSineLastLog = System.currentTimeMillis() - lastLogTime;
        if (elapsedTimeSineLastLog > 10_000) {
            logData();
            lastLogTime = System.currentTimeMillis();
        }
    }

    private void logData() {
        final var elapsedTime = System.currentTimeMillis() - startTime;
        logger.info("ElapsedTime: {} - Processed: {} - AverageTimePerIter: {}",
                humanReadableFormat(elapsedTime), counter, humanReadableFormat(elapsedTime / counter));

//        if ((elapsedTime / 10_000) % 3 == 0) {
//            System.out.println("cache = " + FilePathSimilarity.cache.stats().toString());
//            System.out.println("substringCache = " + FilePathSimilarity.substringCache.stats().toString());
//            System.out.println("subsequenceCache = " + FilePathSimilarity.subsequenceCache.stats().toString());
//        }
    }

    public static String humanReadableFormat(long millis) {
        return Duration.ofMillis(millis).toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }
}
