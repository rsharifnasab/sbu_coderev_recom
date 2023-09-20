package ir.msdehghan.revrec.framework.strategy;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MovingStrategy<T> implements ValidationStrategy<T> {

    private static final Logger logger = LogManager.getLogger(MovingStrategy.class);

    private final int stepSize;
    private final boolean sliding;

    protected final LinkedList<T> trainData = new LinkedList<>();
    private final List<T> unmodifiableTrainData = Collections.unmodifiableList(this.trainData);
    protected final LinkedList<T> testData = new LinkedList<>();
    private final List<T> unmodifiableTestData = Collections.unmodifiableList(testData);
    protected final LinkedList<T> dataToRemove = new LinkedList<>();
    private final List<T> unmodifiableDataToRemove = Collections.unmodifiableList(dataToRemove);
    protected final LinkedList<T> dataToAdd = new LinkedList<>();
    private final List<T> unmodifiableDataToAdd = Collections.unmodifiableList(dataToAdd);
    private final LinkedList<T> testDataToCome;

    private boolean dataProcessed = true;

    public MovingStrategy(List<T> data, int trainWindowSize, int testWindowSize, int stepSize, boolean sliding) {
        Validate.isTrue(data.size() >= trainWindowSize + testWindowSize,
                "Data size must be greater than (trainWindowSize + testWindowSize)");
        Validate.isTrue(testWindowSize >= stepSize, "Test window must be greater than step size");

        this.sliding = sliding;
        this.stepSize = stepSize;

        trainData.addAll(data.subList(0, trainWindowSize));
        testData.addAll(data.subList(trainWindowSize, trainWindowSize + testWindowSize));
        testDataToCome = new LinkedList<>(data.subList(trainWindowSize + testWindowSize, data.size()));

        logger.info("Initial sizes -> Train: {} - Test: {}, Step size: {}, Sliding: {}",
                trainData.size(), testData.size(), stepSize, sliding);
        logger.info("Test data to come: {}", testDataToCome.size());
    }

    @Override
    public List<T> getTrainData() {
        return unmodifiableTrainData;
    }

    @Override
    public List<T> getTestData() {
        if (!dataProcessed) {
            throw new IllegalStateException("Previous data is not processed yet");
        }

        if (testData.isEmpty()) {
            throw new IllegalStateException("There is no test data left!");
        }
        dataProcessed = false;

        return unmodifiableTestData;
    }

    @Override
    public boolean hasTestData() {
        return !testData.isEmpty();
    }

    @Override
    public List<T> getRowsToRemoveFromTrain() {
        if (!dataProcessed) {
            throw new IllegalStateException("Data must be processed first");
        }
        return unmodifiableDataToRemove;
    }

    @Override
    public List<T> getRowsToAddToTrain() {
        if (!dataProcessed) {
            throw new IllegalStateException("Data must be processed first");
        }
        return unmodifiableDataToAdd;
    }

    @Override
    public void testDataProcessed() {
        if (dataProcessed) {
            throw new IllegalStateException("Data is processed already");
        }
        dataProcessed = true;
        dataToRemove.clear();
        dataToAdd.clear();
        if (testDataToCome.isEmpty()) {
            testData.clear();
            return;
        }

        int count = 0;
        while (count < stepSize) {
            if (!testDataToCome.isEmpty()) {
                testData.addLast(testDataToCome.removeFirst());
            }
            final var dataToAdd = testData.removeFirst();
            trainData.addLast(dataToAdd);
            this.dataToAdd.addLast(dataToAdd);
            if (sliding) {
                removeDataFromTrainForSliding();
            }
            count++;
        }
    }

    protected void removeDataFromTrainForSliding() {
        dataToRemove.addLast(trainData.removeFirst());
    }

    public static <T> MovingStrategy<T> slidingOneByOne(List<T> data, int trainWindowSize) {
        return new MovingStrategy<>(data, trainWindowSize, 1, 1, true);
    }

    public static <T> MovingStrategy<T> slidingBatchByBatch(List<T> data, int trainWindowSize, int testWindowSize) {
        return new MovingStrategy<>(data, trainWindowSize, testWindowSize, testWindowSize, true);
    }

    public static <T> MovingStrategy<T> incrementalOneByOne(List<T> data, int trainWindowSize) {
        return new MovingStrategy<>(data, trainWindowSize, 1, 1, false);
    }

    public static <T> MovingStrategy<T> incrementalBatchByBatch(List<T> data, int trainWindowSize, int testWindowSize) {
        return new MovingStrategy<>(data, trainWindowSize, testWindowSize, testWindowSize, false);
    }
}
