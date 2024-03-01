package ir.msdehghan.revrec.framework.strategy;

import org.apache.commons.lang3.Validate;
import java.time.Duration;
import java.util.List;
import java.util.function.ToLongFunction;

public class TemporalMovingStrategy<T> extends MovingStrategy<T> {
    private final long durationMillis;
    private final ToLongFunction<T> timeExtractor;

    private TemporalMovingStrategy(List<T> data, int trainWindowSize, long durationMillis, ToLongFunction<T> timeExtractor) {
        super(data, trainWindowSize, 1, 1, true);
        this.durationMillis = durationMillis;
        this.timeExtractor = timeExtractor;
    }

    public static <T> TemporalMovingStrategy<T> create(List<T> data, Duration trainDuration, ToLongFunction<T> timeExtractor) {
        Validate.notEmpty(data, "Data can't be empty");
        final long startTime = timeExtractor.applyAsLong(data.get(0));
        final long durationMillis = trainDuration.toMillis();

        int count = 1; // The 0 index is must be included.
        while (count < data.size()) {
            long time = timeExtractor.applyAsLong(data.get(count));
            if (time - startTime > durationMillis) {
                break;
            }
            count++;
        }

        int trainSize = count - 1; // We use the last found item as the first test row.
        return new TemporalMovingStrategy<>(data, trainSize, durationMillis, timeExtractor);
    }

    @Override
    protected void removeDataFromTrainForSliding() {
        long currentTime = timeExtractor.applyAsLong(testData.getFirst());
        while (!trainData.isEmpty() && currentTime - timeExtractor.applyAsLong(trainData.getFirst()) > durationMillis) {
            dataToRemove.add(trainData.removeFirst());
        }

        Validate.notEmpty(trainData, "There is no train data after removing stale values!" +
                " test_time:%d - test_data:%s", currentTime, testData.getFirst());
    }
}
